package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.FoodReview;
import com.bylw.mapper.FoodReviewMapper;
import com.bylw.service.FoodReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FoodReviewServiceImpl implements FoodReviewService {

    @Autowired
    private FoodReviewMapper reviewMapper;

    @Override
    public FoodReview addReview(FoodReview review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (review.getContent().length() > 500) {
            throw new IllegalArgumentException("评论内容不能超过500字");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            review.setRating(5);
        }
        review.setCreateTime(LocalDateTime.now());
        review.setDeleted(0);
        reviewMapper.insert(review);
        return review;
    }

    @Override
    public Page<FoodReview> getReviewsByFoodId(Integer foodId, Integer pageNum, Integer pageSize) {
        Page<FoodReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoodReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodReview::getFoodId, foodId)
               .eq(FoodReview::getDeleted, 0)
               .orderByDesc(FoodReview::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<FoodReview> listAllReviews(Integer pageNum, Integer pageSize) {
        Page<FoodReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoodReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodReview::getDeleted, 0)
               .orderByDesc(FoodReview::getCreateTime);
        return reviewMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean deleteReview(Integer reviewId) {
        FoodReview review = reviewMapper.selectById(reviewId);
        if (review != null) {
            review.setDeleted(1);
            reviewMapper.updateById(review);
            return true;
        }
        return false;
    }
}
