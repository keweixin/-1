package com.bylw.service;

/**
 * 趋势分析服务 - 提供环保指标、浪费指数、积分产出等趋势分析
 */
public interface TrendAnalysisService {
    /**
     * 食品节约量趋势率（本周 vs 上周，正数表示改善）
     */
    Double calculateFoodSavedTrendRate();

    /**
     * 浪费指数趋势率（本周 vs 上周，负数表示改善）
     */
    Double calculateWasteIndexTrendRate();

    /**
     * 积分产出趋势率（本周 vs 上周）
     */
    Double calculatePointsTrendRate();
}