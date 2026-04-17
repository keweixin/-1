package com.bylw.service;

import com.bylw.dto.RecommendResultDTO;
import com.bylw.entity.*;

import java.util.List;

/**
 * 推荐服务接口 - 包含推荐算法、行为记录、文章食谱推荐功能
 * 论文公式：
 *   公式2-1（余弦相似度）: sim(u,v) = Σ(i∈I∩v) ru,i·rv,i / (√Σi∈u ru,i² × √Σi∈v rv,i²)
 *   公式2-2（加权融合）:    Score(u,j) = α·CF(u,j) + β·FM(u,j), α+β=1
 *   时间衰减: w(t) = w₀ × exp(-λ·Δt), Δt=距今天数, λ=0.1（半衰期约7天）
 */
public interface RecommendService {
    /**
     * 获取推荐食品列表（协同过滤 + 特征匹配混合推荐）
     */
    RecommendResultDTO getRecommendFoods(Integer userId, int limit);

    /**
     * 获取推荐文章
     */
    List<Article> getRecommendArticles(Integer userId, int limit);

    /**
     * 获取推荐食谱
     */
    List<Recipe> getRecommendRecipes(Integer userId, int limit);

    /**
     * 记录用户行为
     */
    void recordBehavior(Integer userId, String targetType, Integer targetId, String behaviorType);

    /**
     * 检查用户是否已收藏
     */
    boolean isFavorited(Integer userId, String targetType, Integer targetId);

    /**
     * 切换收藏状态（已收藏则取消，未收藏则添加）
     * @return true=已收藏, false=已取消收藏
     */
    boolean toggleFavorite(Integer userId, String targetType, Integer targetId);
}