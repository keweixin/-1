package com.bylw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.entity.FoodReview;
import com.bylw.service.FoodReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class FoodReviewController {

    @Autowired
    private FoodReviewService reviewService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping
    public Result<?> addReview(HttpServletRequest request, @RequestBody FoodReview review) {
        Integer userId = authUtil.getUserId(request);
        review.setUserId(userId);
        return Result.success(reviewService.addReview(review));
    }

    @GetMapping("/food/{foodId}")
    public Result<?> getReviewsByFoodId(@PathVariable Integer foodId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.getReviewsByFoodId(foodId, pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<?> listAllReviews(HttpServletRequest request,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        authUtil.verifyAdmin(request);
        return Result.success(reviewService.listAllReviews(pageNum, pageSize));
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteReview(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(reviewService.deleteReview(id));
    }
}
