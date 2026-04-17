package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.common.Constants;
import com.bylw.entity.Food;
import com.bylw.entity.Order;
import com.bylw.entity.PointsRecord;
import com.bylw.mapper.OrderMapper;
import com.bylw.mapper.PointsRecordMapper;
import com.bylw.service.TrendAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 趋势分析服务实现 - 专注于环保指标、浪费指数、积分产出等趋势计算
 */
@Service
public class TrendAnalysisServiceImpl implements TrendAnalysisService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Override
    public Double calculateFoodSavedTrendRate() {
        int thisWeek = calculateFoodSavedKgThisWeek();
        int lastWeek = calculateFoodSavedKgLastWeek();
        return calculateChangeRate(thisWeek, lastWeek);
    }

    @Override
    public Double calculateWasteIndexTrendRate() {
        double thisWeek = calculateWasteIndexThisWeek();
        double lastWeek = calculateWasteIndexLastWeek();
        if (lastWeek <= 0) {
            return thisWeek > 0 ? 100.0 : 0.0;
        }
        double diff = thisWeek - lastWeek;
        return Math.round(diff / lastWeek * 100 * 10.0) / 10.0;
    }

    @Override
    public Double calculatePointsTrendRate() {
        int thisWeek = calculatePointsEarnedThisWeek();
        int lastWeek = calculatePointsEarnedLastWeek();
        return calculateChangeRate(thisWeek, lastWeek);
    }

    private int calculateFoodSavedKgThisWeek() {
        return calculateOrderItemQtyBetween(daysAgoStart(6), tomorrowStart(), Constants.ORDER_STATUS_COMPLETED);
    }

    private int calculateFoodSavedKgLastWeek() {
        return calculateOrderItemQtyBetween(daysAgoStart(13), daysAgoStart(6), Constants.ORDER_STATUS_COMPLETED);
    }

    private double calculateWasteIndexThisWeek() {
        return calculateWasteIndexBetween(daysAgoStart(6), tomorrowStart());
    }

    private double calculateWasteIndexLastWeek() {
        return calculateWasteIndexBetween(daysAgoStart(13), daysAgoStart(6));
    }

    private int calculateOrderItemQtyBetween(LocalDateTime start, LocalDateTime end, String status) {
        return orderMapper.selectTotalQuantityByStatusAndTimeRange(status, start, end);
    }

    private double calculateWasteIndexBetween(LocalDateTime start, LocalDateTime end) {
        int saved = calculateOrderItemQtyBetween(start, end, Constants.ORDER_STATUS_COMPLETED);
        int wasted = orderMapper.selectWastedQuantityByTimeRange(start, end);
        int total = saved + wasted;
        if (total == 0) {
            return 0.0;
        }
        return Math.round((wasted * 1.0 / total) * 100 * 100.0) / 100.0;
    }

    private int calculatePointsEarnedThisWeek() {
        LocalDateTime start = daysAgoStart(6);
        LocalDateTime end = tomorrowStart();
        return pointsRecordMapper.selectSumByChangeTypeAndTimeRange(Constants.CHANGE_TYPE_EARN, start, end);
    }

    private int calculatePointsEarnedLastWeek() {
        LocalDateTime start = daysAgoStart(13);
        LocalDateTime end = daysAgoStart(6);
        return pointsRecordMapper.selectSumByChangeTypeAndTimeRange(Constants.CHANGE_TYPE_EARN, start, end);
    }

    private LocalDateTime daysAgoStart(int daysAgo) {
        return LocalDate.now().minusDays(daysAgo).atStartOfDay();
    }

    private LocalDateTime tomorrowStart() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }

    private double calculateChangeRate(int current, int previous) {
        if (previous <= 0) {
            return current <= 0 ? 0D : 100D;
        }
        BigDecimal diff = BigDecimal.valueOf(current - previous);
        return diff.multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
