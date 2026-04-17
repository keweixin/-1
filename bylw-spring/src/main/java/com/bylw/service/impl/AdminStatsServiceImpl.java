package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.common.Constants;
import com.bylw.dto.AdminStatsDTO;
import com.bylw.entity.*;
import com.bylw.mapper.*;
import com.bylw.service.AdminStatsService;
import com.bylw.service.TrendAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员统计服务实现 - 提供后台仪表盘所需的各种统计数据
 * 论文公式：
 *   公式2-3（趋势率）: Rate = (本期-上期)/上期 × 100%
 *   公式2-4（浪费指数）: WasteIndex = 退款数量/(完成数量+退款数量) × 100%
 */
@Service
public class AdminStatsServiceImpl implements AdminStatsService {

    @Autowired
    private FoodMapper foodMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AfterSaleMapper afterSaleMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private PointsExchangeMapper exchangeMapper;
    @Autowired
    private PointsRecordMapper pointsRecordMapper;
    @Autowired
    private TrendAnalysisService trendAnalysisService;

    @Override
    public AdminStatsDTO getAdminStats() {
        AdminStatsDTO stats = new AdminStatsDTO();
        // 基础统计
        stats.setFoodCount(countActiveFoods());
        stats.setOrderCount(countOrders());
        stats.setUserCount(countUsers());
        stats.setAppealCount(countPendingAppeals());
        stats.setPostCount(countPosts());
        stats.setPointsExchange(countPointsExchanges());
        stats.setInactiveStoreCount(countInactiveStores());
        stats.setLowStockFoodCount(countLowStockFoods());
        stats.setPendingPostCount(countPendingPosts());
        // 趋势率
        stats.setFoodTrendRate(calculateFoodTrendRate());
        stats.setOrderTrendRate(calculateOrderTrendRate());
        stats.setUserTrendRate(calculateUserTrendRate());
        stats.setAppealTrendRate(calculateAppealTrendRate());
        stats.setPostTrendRate(calculatePostTrendRate());
        stats.setPointsExchangeTrendRate(calculatePointsExchangeTrendRate());
        // 图表数据
        stats.setTrendData(buildTrendData());
        stats.setCategoryData(buildCategoryData());
        // 环保指标
        stats.setFoodSavedKg(calculateFoodSavedKg());
        stats.setTotalPointsEarned(calculateTotalPointsEarned());
        stats.setAvgOrderValue(calculateAvgOrderValue());
        stats.setCompletedOrders(calculateCompletedOrders());
        stats.setWasteIndex(calculateWasteIndex());
        // 环保指标趋势（委托 TrendAnalysisService）
        stats.setFoodSavedTrendRate(trendAnalysisService.calculateFoodSavedTrendRate());
        stats.setWasteIndexTrendRate(trendAnalysisService.calculateWasteIndexTrendRate());
        stats.setPointsTrendRate(trendAnalysisService.calculatePointsTrendRate());
        return stats;
    }

    private List<Map<String, Object>> buildTrendData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate day = LocalDate.now().minusDays(offset);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", day.format(formatter));
            item.put("food", countFoodCreatedBetween(start, end));
            item.put("orders", countPaidOrdersBetween(start, end));
            trendData.add(item);
        }
        return trendData;
    }

    private List<Map<String, Object>> buildCategoryData() {
        LinkedHashMap<String, Integer> counter = new LinkedHashMap<>();
        counter.put(Constants.REASON_QUALITY, 0);
        counter.put(Constants.REASON_DELAY, 0);
        counter.put(Constants.REASON_DAMAGE, 0);
        counter.put(Constants.REASON_OTHER, 0);

        List<Map<String, Object>> rawStats = afterSaleMapper.selectReasonTypeStats();
        for (Map<String, Object> row : rawStats) {
            String reason = row.get("reasonType") == null ? Constants.REASON_OTHER : (String) row.get("reasonType");
            int cnt = ((Number) row.get("cnt")).intValue();
            if (reason == null || reason.isBlank()) {
                reason = Constants.REASON_OTHER;
            }
            counter.merge(reason, cnt, Integer::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            result.add(item);
        }
        return result;
    }

    private int countActiveFoods() {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getDeleted, 0).eq(Food::getStatus, 1);
        return Math.toIntExact(foodMapper.selectCount(wrapper));
    }

    private int countOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        return Math.toIntExact(orderMapper.selectCount(wrapper));
    }

    private int countUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0).eq(User::getRole, Constants.ROLE_USER);
        return Math.toIntExact(userMapper.selectCount(wrapper));
    }

    private int countPendingAppeals() {
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSale::getHandleStatus, Constants.HANDLE_STATUS_PENDING);
        return Math.toIntExact(afterSaleMapper.selectCount(wrapper));
    }

    private int countPosts() {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 0);
        return Math.toIntExact(postMapper.selectCount(wrapper));
    }

    private int countPointsExchanges() {
        return Math.toIntExact(exchangeMapper.selectCount(null));
    }

    private int countInactiveStores() {
        return 0;
    }

    private int countLowStockFoods() {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getDeleted, 0).eq(Food::getStatus, 1).le(Food::getStockQty, 10);
        return Math.toIntExact(foodMapper.selectCount(wrapper));
    }

    private int countPendingPosts() {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 0).eq(Post::getAuditStatus, Constants.AUDIT_STATUS_PENDING);
        return Math.toIntExact(postMapper.selectCount(wrapper));
    }

    private Double calculateFoodTrendRate() {
        return calculateChangeRate(
                countFoodCreatedBetween(daysAgoStart(6), tomorrowStart()),
                countFoodCreatedBetween(daysAgoStart(13), daysAgoStart(6))
        );
    }

    private Double calculateOrderTrendRate() {
        return calculateChangeRate(
                countPaidOrdersBetween(daysAgoStart(6), tomorrowStart()),
                countPaidOrdersBetween(daysAgoStart(13), daysAgoStart(6))
        );
    }

    private Double calculateUserTrendRate() {
        return calculateChangeRate(
                countUsersCreatedBetween(daysAgoStart(6), tomorrowStart()),
                countUsersCreatedBetween(daysAgoStart(13), daysAgoStart(6))
        );
    }

    private Double calculateAppealTrendRate() {
        return calculateChangeRate(
                countAppealsAppliedBetween(daysAgoStart(6), tomorrowStart()),
                countAppealsAppliedBetween(daysAgoStart(13), daysAgoStart(6))
        );
    }

    private Double calculatePostTrendRate() {
        return calculateChangeRate(
                countPostsCreatedBetween(daysAgoStart(6), tomorrowStart()),
                countPostsCreatedBetween(daysAgoStart(13), daysAgoStart(6))
        );
    }

    private Double calculatePointsExchangeTrendRate() {
        return calculateChangeRate(
                countPointsExchangeBetween(daysAgoStart(6), tomorrowStart()),
                countPointsExchangeBetween(daysAgoStart(13), daysAgoStart(6))
        );
    }

    private int countFoodCreatedBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getDeleted, 0).eq(Food::getStatus, 1)
                .ge(Food::getCreateTime, start).lt(Food::getCreateTime, end);
        return Math.toIntExact(foodMapper.selectCount(wrapper));
    }

    private int countPaidOrdersBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0).eq(Order::getPayStatus, Constants.PAY_STATUS_PAID)
                .ge(Order::getPayTime, start).lt(Order::getPayTime, end);
        return Math.toIntExact(orderMapper.selectCount(wrapper));
    }

    private int countUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0).eq(User::getRole, Constants.ROLE_USER)
                .ge(User::getCreateTime, start).lt(User::getCreateTime, end);
        return Math.toIntExact(userMapper.selectCount(wrapper));
    }

    private int countAppealsAppliedBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(AfterSale::getApplyTime, start).lt(AfterSale::getApplyTime, end);
        return Math.toIntExact(afterSaleMapper.selectCount(wrapper));
    }

    private int countPostsCreatedBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 0)
                .ge(Post::getCreateTime, start).lt(Post::getCreateTime, end);
        return Math.toIntExact(postMapper.selectCount(wrapper));
    }

    private int countPointsExchangeBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(PointsExchange::getCreateTime, start).lt(PointsExchange::getCreateTime, end);
        return Math.toIntExact(exchangeMapper.selectCount(wrapper));
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

    private Double calculateFoodSavedKg() {
        Integer totalQuantity = orderMapper.selectTotalQuantityByOrderStatus(Constants.ORDER_STATUS_COMPLETED);
        return (totalQuantity != null ? totalQuantity : 0) * 0.5;
    }

    private Integer calculateTotalPointsEarned() {
        return pointsRecordMapper.selectSumByChangeType(Constants.CHANGE_TYPE_EARN);
    }

    private Double calculateAvgOrderValue() {
        Double avg = orderMapper.selectAvgTotalAmountByStatus(Constants.ORDER_STATUS_COMPLETED);
        return Math.round((avg != null ? avg : 0.0) * 100.0) / 100.0;
    }

    private Integer calculateCompletedOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderStatus, Constants.ORDER_STATUS_COMPLETED);
        return Math.toIntExact(orderMapper.selectCount(wrapper));
    }

    private Double calculateWasteIndex() {
        Integer savedQty = orderMapper.selectTotalQuantityByOrderStatus(Constants.ORDER_STATUS_COMPLETED);
        Integer wastedQty = orderMapper.selectTotalQuantityByOrderStatus(Constants.ORDER_STATUS_REFUNDED);
        int total = savedQty + wastedQty;
        if (total == 0) {
            return 0.0;
        }
        double index = (wastedQty * 1.0 / total) * 100;
        return Math.round(index * 100.0) / 100.0;
    }
}
