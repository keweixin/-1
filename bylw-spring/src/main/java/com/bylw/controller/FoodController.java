package com.bylw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.entity.Food;
import com.bylw.entity.FoodCategory;
import com.bylw.entity.FoodTag;
import com.bylw.service.FoodService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minExpiryDays,
            @RequestParam(required = false) Integer maxExpiryDays) {
        return Result.success(foodService.listPage(pageNum, pageSize, keyword, categoryId, minPrice, maxPrice, minExpiryDays, maxExpiryDays));
    }

    @GetMapping("/admin/list")
    public Result<?> adminList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.adminListPage(pageNum, pageSize, keyword, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        return Result.success(foodService.getById(id));
    }

    @GetMapping("/categories")
    public Result<?> getCategories() {
        return Result.success(foodService.getCategories());
    }

    @GetMapping("/tags")
    public Result<?> getTags() {
        return Result.success(foodService.getTags());
    }

    @PostMapping
    public Result<?> save(HttpServletRequest request, @RequestBody Food food) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.save(food));
    }

    @PutMapping
    public Result<?> update(HttpServletRequest request, @RequestBody Food food) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.update(food));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.delete(id));
    }

    @PutMapping("/status/{id}")
    public Result<?> updateStatus(HttpServletRequest request, @PathVariable Integer id, @RequestParam Integer status) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.updateStatus(id, status));
    }

    @PostMapping("/categories")
    public Result<?> saveCategory(HttpServletRequest request, @RequestBody FoodCategory category) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.saveCategory(category));
    }

    @PutMapping("/categories")
    public Result<?> updateCategory(HttpServletRequest request, @RequestBody FoodCategory category) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.updateCategory(category));
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(foodService.deleteCategory(id));
    }
}
