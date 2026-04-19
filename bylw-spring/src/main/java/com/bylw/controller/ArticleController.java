package com.bylw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.entity.Article;
import com.bylw.entity.Recipe;
import com.bylw.entity.RecipeFavorite;
import com.bylw.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private AuthUtil authUtil;

    // 膳食百科
    @GetMapping("/list")
    public Result<?> listArticles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.listArticles(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<?> getArticle(@PathVariable Integer id) {
        articleService.incrementReadCount(id);
        return Result.success(articleService.getArticleById(id));
    }

    @PostMapping
    public Result<?> saveArticle(HttpServletRequest request, @RequestBody Article article) {
        authUtil.verifyAdmin(request);
        return Result.success(articleService.save(article));
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteArticle(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(articleService.deleteArticle(id));
    }

    @PutMapping
    public Result<?> updateArticle(HttpServletRequest request, @RequestBody Article article) {
        authUtil.verifyAdmin(request);
        return Result.success(articleService.updateArticle(article));
    }

    // 健康食谱
    @GetMapping("/recipe/list")
    public Result<?> listRecipes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String suitablePeople) {
        return Result.success(articleService.listRecipes(pageNum, pageSize, keyword, suitablePeople));
    }

    @GetMapping("/recipe/{id}")
    public Result<?> getRecipe(@PathVariable Integer id) {
        return Result.success(articleService.getRecipeById(id));
    }

    @PostMapping("/recipe")
    public Result<?> saveRecipe(HttpServletRequest request, @RequestBody Recipe recipe) {
        authUtil.verifyAdmin(request);
        return Result.success(articleService.saveRecipe(recipe));
    }

    @DeleteMapping("/recipe/{id}")
    public Result<?> deleteRecipe(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(articleService.deleteRecipe(id));
    }

    @PutMapping("/recipe")
    public Result<?> updateRecipe(HttpServletRequest request, @RequestBody Recipe recipe) {
        authUtil.verifyAdmin(request);
        return Result.success(articleService.updateRecipe(recipe));
    }

    @PostMapping("/favorite/{recipeId}")
    public Result<?> toggleFavorite(HttpServletRequest request, @PathVariable Integer recipeId) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(articleService.toggleFavorite(userId, recipeId));
    }

    @GetMapping("/favorite/my")
    public Result<?> myFavorites(HttpServletRequest request) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(articleService.getFavorites(userId));
    }
}