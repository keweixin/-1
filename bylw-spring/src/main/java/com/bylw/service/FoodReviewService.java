package com.bylw.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.FoodReview;

public interface FoodReviewService {
    FoodReview addReview(FoodReview review);
    Page<FoodReview> getReviewsByFoodId(Integer foodId, Integer pageNum, Integer pageSize);
    Page<FoodReview> listAllReviews(Integer pageNum, Integer pageSize);
    boolean deleteReview(Integer reviewId);
}
