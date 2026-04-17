package com.bylw.controller;

import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.config.RecommendConfigHolder;
import com.bylw.config.RecommendConfigHolder.ConfigDTO;
import com.bylw.dto.RecommendResultDTO;
import com.bylw.entity.Article;
import com.bylw.entity.Recipe;
import com.bylw.service.AdminStatsService;
import com.bylw.service.RecommendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private AdminStatsService adminStatsService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private RecommendConfigHolder configHolder;

    @GetMapping("/stats")
    public Result<?> getAdminStats(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        return Result.success(adminStatsService.getAdminStats());
    }

    @GetMapping("/foods")
    public Result<?> getFoods(HttpServletRequest request,
                               @RequestParam(defaultValue = "10") Integer limit) {
        Integer userId = getUserIdOptional(request);
        return Result.success(recommendService.getRecommendFoods(userId, limit));
    }

    @GetMapping("/articles")
    public Result<?> getArticles(HttpServletRequest request,
                                  @RequestParam(defaultValue = "10") Integer limit) {
        Integer userId = getUserIdOptional(request);
        return Result.success(recommendService.getRecommendArticles(userId, limit));
    }

    @GetMapping("/recipes")
    public Result<?> getRecipes(HttpServletRequest request,
                                 @RequestParam(defaultValue = "10") Integer limit) {
        Integer userId = getUserIdOptional(request);
        return Result.success(recommendService.getRecommendRecipes(userId, limit));
    }

    private Integer getUserIdOptional(HttpServletRequest request) {
        try {
            return authUtil.getUserId(request);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/behavior")
    public Result<?> recordBehavior(HttpServletRequest request,
                                     @RequestBody java.util.Map<String, Object> body) {
        Integer userId = authUtil.getUserId(request);
        String targetType = (String) body.get("targetType");
        Integer targetId = (Integer) body.get("targetId");
        String behaviorType = (String) body.get("behaviorType");
        recommendService.recordBehavior(userId, targetType, targetId, behaviorType);
        return Result.success(null);
    }

    @GetMapping("/favorite/check")
    public Result<?> checkFavorite(HttpServletRequest request,
                                    @RequestParam String targetType,
                                    @RequestParam Integer targetId) {
        Integer userId;
        try {
            userId = authUtil.getUserId(request);
        } catch (Exception e) {
            return Result.success(false);
        }
        boolean favorited = recommendService.isFavorited(userId, targetType, targetId);
        return Result.success(favorited);
    }

    @PostMapping("/favorite/toggle")
    public Result<?> toggleFavorite(HttpServletRequest request,
                                     @RequestBody java.util.Map<String, Object> body) {
        Integer userId = authUtil.getUserId(request);
        String targetType = (String) body.get("targetType");
        Integer targetId = (Integer) body.get("targetId");
        boolean favorited = recommendService.toggleFavorite(userId, targetType, targetId);
        return Result.success(favorited);
    }

    @GetMapping("/config")
    public Result<?> getConfig(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        ConfigDTO config = new ConfigDTO(
            configHolder.getAlpha(),
            configHolder.getBeta(),
            configHolder.getLambda(),
            configHolder.getBehaviorWindowDays()
        );
        return Result.success(config);
    }

    @PutMapping("/config")
    public Result<?> updateConfig(HttpServletRequest request,
                                  @RequestBody ConfigDTO config) {
        authUtil.verifyAdmin(request);
        configHolder.updateConfig(config);
        return Result.success(null);
    }
}
