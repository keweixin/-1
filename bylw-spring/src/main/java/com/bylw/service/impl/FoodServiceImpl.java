package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.dto.FoodDTO;
import com.bylw.entity.*;
import com.bylw.mapper.*;
import com.bylw.service.FoodService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodMapper foodMapper;
    @Autowired
    private FoodCategoryMapper categoryMapper;
    @Autowired
    private FoodTagMapper tagMapper;

    @Override
    public Page<FoodDTO> listPage(Integer pageNum, Integer pageSize, String keyword, Integer categoryId,
                                   Double minPrice, Double maxPrice, Integer minExpiryDays, Integer maxExpiryDays) {
        return queryFoodPage(pageNum, pageSize, keyword, categoryId, 1, minPrice, maxPrice, minExpiryDays, maxExpiryDays);
    }

    @Override
    public Page<FoodDTO> adminListPage(Integer pageNum, Integer pageSize, String keyword, Integer categoryId,
                                       Integer status) {
        return queryFoodPage(pageNum, pageSize, keyword, categoryId, status, null, null, null, null);
    }

    private Page<FoodDTO> queryFoodPage(Integer pageNum, Integer pageSize, String keyword, Integer categoryId,
                                        Integer status, Double minPrice, Double maxPrice,
                                        Integer minExpiryDays, Integer maxExpiryDays) {
        Page<Food> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getDeleted, 0)
               .orderByDesc(Food::getCreateTime);

        if (status != null) {
            wrapper.eq(Food::getStatus, status);
        }

        if (categoryId != null) {
            wrapper.eq(Food::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Food::getFoodName, keyword);
        }
        if (minPrice != null) {
            wrapper.ge(Food::getDiscountPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Food::getDiscountPrice, maxPrice);
        }
        if (minExpiryDays != null || maxExpiryDays != null) {
            if (minExpiryDays != null && maxExpiryDays != null) {
                wrapper.apply("DATEDIFF(expire_date, NOW()) >= {0} AND DATEDIFF(expire_date, NOW()) <= {1}",
                             minExpiryDays, maxExpiryDays);
            } else if (minExpiryDays != null) {
                wrapper.apply("DATEDIFF(expire_date, NOW()) >= {0}", minExpiryDays);
            } else {
                wrapper.apply("DATEDIFF(expire_date, NOW()) <= {0}", maxExpiryDays);
            }
        }

        Page<Food> result = foodMapper.selectPage(page, wrapper);
        Page<FoodDTO> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(result.getTotal());
        List<FoodDTO> dtoList = result.getRecords().stream().map(this::toDTO).toList();
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public FoodDTO getById(Integer foodId) {
        Food food = foodMapper.selectById(foodId);
        return food != null ? toDTO(food) : null;
    }

    @Override
    public Food save(Food food) {
        food.setCreateTime(LocalDateTime.now());
        food.setUpdateTime(LocalDateTime.now());
        food.setDeleted(0);
        if (food.getExpireDate() != null && food.getOriginPrice() != null) {
            autoCalculateDiscountAndTags(food);
        }
        foodMapper.insert(food);
        return food;
    }

    @Override
    public Food update(Food food) {
        food.setUpdateTime(LocalDateTime.now());
        foodMapper.updateById(food);
        return foodMapper.selectById(food.getFoodId());
    }

    @Override
    public boolean delete(Integer foodId) {
        Food food = foodMapper.selectById(foodId);
        if (food != null) {
            food.setDeleted(1);
            foodMapper.updateById(food);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Integer foodId, Integer status) {
        Food food = foodMapper.selectById(foodId);
        if (food != null) {
            food.setStatus(status);
            food.setUpdateTime(LocalDateTime.now());
            foodMapper.updateById(food);
            return true;
        }
        return false;
    }

    @Override
    public List<FoodCategory> getCategories() {
        LambdaQueryWrapper<FoodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodCategory::getStatus, 1)
               .eq(FoodCategory::getDeleted, 0)
               .orderByAsc(FoodCategory::getSortNo);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<FoodTag> getTags() {
        LambdaQueryWrapper<FoodTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodTag::getStatus, 1)
               .eq(FoodTag::getDeleted, 0);
        return tagMapper.selectList(wrapper);
    }

    @Override
    public FoodCategory saveCategory(FoodCategory category) {
        category.setDeleted(0);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public FoodCategory updateCategory(FoodCategory category) {
        categoryMapper.updateById(category);
        return categoryMapper.selectById(category.getCategoryId());
    }

    @Override
    public boolean deleteCategory(Integer categoryId) {
        FoodCategory category = categoryMapper.selectById(categoryId);
        if (category != null) {
            category.setDeleted(1);
            categoryMapper.updateById(category);
            return true;
        }
        return false;
    }

    private FoodDTO toDTO(Food food) {
        FoodDTO dto = new FoodDTO();
        BeanUtils.copyProperties(food, dto);

        FoodCategory category = categoryMapper.selectById(food.getCategoryId());
        if (category != null) dto.setCategoryName(category.getCategoryName());

        return dto;
    }

    /**
     * 根据到期时间自动计算折扣价并生成标签
     * 折扣规则：
     *   <12小时 → 3折（0.3）
     *   12-24小时 → 5折（0.5）
     *   24-48小时 → 7折（0.7）
     *   48-72小时 → 8折（0.8）
     *   >72小时 → 不打折（1.0）
     */
    private void autoCalculateDiscountAndTags(Food food) {
        long hoursLeft = ChronoUnit.HOURS.between(LocalDateTime.now(), food.getExpireDate());
        if (hoursLeft < 0) {
            return;
        }
        BigDecimal discountRate;
        String discountTag;
        if (hoursLeft < 12) {
            discountRate = new BigDecimal("0.3");
            discountTag = "今日特惠";
        } else if (hoursLeft < 24) {
            discountRate = new BigDecimal("0.5");
            discountTag = "临期特惠";
        } else if (hoursLeft < 48) {
            discountRate = new BigDecimal("0.7");
            discountTag = "低价促销";
        } else if (hoursLeft < 72) {
            discountRate = new BigDecimal("0.8");
            discountTag = "即将到期";
        } else {
            discountRate = new BigDecimal("1.0");
            discountTag = null;
        }
        if (discountRate.compareTo(new BigDecimal("1.0")) < 0) {
            food.setDiscountPrice(food.getOriginPrice().multiply(discountRate).setScale(2, RoundingMode.HALF_UP));
        }
        if (discountTag != null && food.getTasteDesc() != null && !food.getTasteDesc().contains(discountTag)) {
            food.setTasteDesc(discountTag + "，" + food.getTasteDesc());
        } else if (discountTag != null && food.getTasteDesc() == null) {
            food.setTasteDesc(discountTag);
        }
    }
}
