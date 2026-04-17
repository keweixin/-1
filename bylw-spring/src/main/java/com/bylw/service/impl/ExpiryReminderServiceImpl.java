package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.common.Constants;
import com.bylw.entity.Food;
import com.bylw.entity.Order;
import com.bylw.entity.OrderItem;
import com.bylw.mapper.FoodMapper;
import com.bylw.mapper.OrderItemMapper;
import com.bylw.mapper.OrderMapper;
import com.bylw.service.ExpiryReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExpiryReminderServiceImpl implements ExpiryReminderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private FoodMapper foodMapper;

    @Override
    public List<Food> getExpiringFoods(Integer userId, int hoursAhead) {
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                   .eq(Order::getOrderStatus, Constants.ORDER_STATUS_COMPLETED);
        List<Order> completedOrders = orderMapper.selectList(orderWrapper);
        if (completedOrders.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> orderIds = completedOrders.stream().map(Order::getOrderId).collect(Collectors.toList());
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        Set<Integer> foodIds = items.stream().map(OrderItem::getFoodId).collect(Collectors.toSet());
        if (foodIds.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(hoursAhead);
        LambdaQueryWrapper<Food> foodWrapper = new LambdaQueryWrapper<>();
        foodWrapper.in(Food::getFoodId, foodIds)
                   .le(Food::getExpireDate, threshold)
                   .gt(Food::getExpireDate, now)
                   .eq(Food::getStatus, 1)
                   .orderByAsc(Food::getExpireDate);
        return foodMapper.selectList(foodWrapper);
    }
}
