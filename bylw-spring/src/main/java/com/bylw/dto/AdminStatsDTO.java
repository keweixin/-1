package com.bylw.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AdminStatsDTO {
    private Integer foodCount;
    private Integer orderCount;
    private Integer userCount;
    private Integer appealCount;
    private Integer postCount;
    private Integer pointsExchange;
    private Integer inactiveStoreCount;
    private Integer lowStockFoodCount;
    private Integer pendingPostCount;
    private Double foodTrendRate;
    private Double orderTrendRate;
    private Double userTrendRate;
    private Double appealTrendRate;
    private Double postTrendRate;
    private Double pointsExchangeTrendRate;
    private List<Map<String, Object>> trendData;
    private List<Map<String, Object>> categoryData;
    private Double foodSavedKg;
    private Integer totalPointsEarned;
    private Double avgOrderValue;
    private Integer completedOrders;
    private Double wasteIndex;
    /** 本周 vs 上周：食品节约量变化率 */
    private Double foodSavedTrendRate;
    /** 本周 vs 上周：浪费指数变化率（负数表示改善） */
    private Double wasteIndexTrendRate;
    /** 本周 vs 上周：积分产出变化率 */
    private Double pointsTrendRate;
}
