package com.bylw.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.dto.FoodDTO;
import com.bylw.entity.Food;
import com.bylw.entity.FoodCategory;
import com.bylw.entity.FoodTag;

import java.util.List;

public interface FoodService {
    Page<FoodDTO> listPage(Integer pageNum, Integer pageSize, String keyword, Integer categoryId, Double minPrice, Double maxPrice, Integer minExpiryDays, Integer maxExpiryDays);
    Page<FoodDTO> adminListPage(Integer pageNum, Integer pageSize, String keyword, Integer categoryId, Integer status);
    FoodDTO getById(Integer foodId);
    Food save(Food food);
    Food update(Food food);
    boolean delete(Integer foodId);
    boolean updateStatus(Integer foodId, Integer status);
    List<FoodCategory> getCategories();
    List<FoodTag> getTags();
    FoodCategory saveCategory(FoodCategory category);
    FoodCategory updateCategory(FoodCategory category);
    boolean deleteCategory(Integer categoryId);
}
