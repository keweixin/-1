package com.bylw.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.Result;
import com.bylw.entity.Food;
import com.bylw.entity.Order;
import com.bylw.entity.OrderItem;
import com.bylw.mapper.FoodMapper;
import com.bylw.mapper.OrderItemMapper;
import com.bylw.mapper.OrderMapper;
import com.bylw.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    @Autowired
    private FoodMapper foodMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/foods")
    public Result<?> listMyFoods(HttpServletRequest request,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer merchantId = getMerchantId(request);

        Page<Food> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getMerchantId, merchantId)
               .orderByDesc(Food::getCreateTime);
        return Result.success(foodMapper.selectPage(page, wrapper));
    }

    @GetMapping("/orders")
    public Result<?> listMyOrders(HttpServletRequest request,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer merchantId = getMerchantId(request);

        // Find all food IDs belonging to this merchant
        LambdaQueryWrapper<Food> foodWrapper = new LambdaQueryWrapper<>();
        foodWrapper.eq(Food::getMerchantId, merchantId)
                   .select(Food::getFoodId);
        List<Integer> myFoodIds = foodMapper.selectList(foodWrapper)
            .stream().map(Food::getFoodId).toList();

        if (myFoodIds.isEmpty()) {
            return Result.success(new Page<>(pageNum, pageSize));
        }

        // Find order items containing merchant's foods
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getFoodId, myFoodIds)
                   .select(OrderItem::getOrderId);
        List<Integer> orderIds = orderItemMapper.selectList(itemWrapper)
            .stream().map(OrderItem::getOrderId).distinct().toList();

        if (orderIds.isEmpty()) {
            return Result.success(new Page<>(pageNum, pageSize));
        }

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Order::getOrderId, orderIds)
                    .orderByDesc(Order::getCreateTime);
        return Result.success(orderMapper.selectPage(page, orderWrapper));
    }

    @GetMapping("/stats")
    public Result<?> getMyStats(HttpServletRequest request) {
        Integer merchantId = getMerchantId(request);

        LambdaQueryWrapper<Food> foodWrapper = new LambdaQueryWrapper<>();
        foodWrapper.eq(Food::getMerchantId, merchantId);
        long totalFoods = foodMapper.selectCount(foodWrapper);

        LambdaQueryWrapper<Food> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Food::getMerchantId, merchantId).eq(Food::getStatus, 1).eq(Food::getDeleted, 0);
        long activeFoods = foodMapper.selectCount(activeWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFoods", totalFoods);
        stats.put("activeFoods", activeFoods);
        stats.put("merchantId", merchantId);
        return Result.success(stats);
    }

    @PutMapping("/orders/accept/{id}")
    public Result<?> acceptOrder(HttpServletRequest request, @PathVariable Integer id) {
        Integer merchantId = getMerchantId(request);
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"待接单".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("当前订单状态不可接单");
        }
        // 校验订单中是否包含该商家的商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, id).select(OrderItem::getFoodId);
        List<Integer> foodIds = orderItemMapper.selectList(itemWrapper)
            .stream().map(OrderItem::getFoodId).toList();
        if (!foodIds.isEmpty()) {
            LambdaQueryWrapper<Food> foodWrapper = new LambdaQueryWrapper<>();
            foodWrapper.in(Food::getFoodId, foodIds).eq(Food::getMerchantId, merchantId);
            if (foodMapper.selectCount(foodWrapper) == 0) {
                throw new IllegalArgumentException("无权接此订单，订单中不包含您的商品");
            }
        }
        order.setOrderStatus("待配送");
        orderMapper.updateById(order);
        return Result.success(true);
    }

    private Integer getMerchantId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未登录，请先获取令牌");
        }
        String token = authHeader.replace("Bearer ", "");
        Integer roleType = jwtUtil.getRoleType(token);
        if (roleType == null || roleType != 3) {
            throw new IllegalArgumentException("权限不足，仅商户可访问");
        }
        return jwtUtil.getUserId(token);
    }
}
