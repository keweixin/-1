package com.bylw.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bylw.common.Constants;
import com.bylw.entity.Order;
import com.bylw.entity.OrderItem;
import com.bylw.entity.Food;
import com.bylw.mapper.OrderMapper;
import com.bylw.mapper.OrderItemMapper;
import com.bylw.mapper.FoodMapper;
import com.bylw.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTimeoutTask {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private PointsService pointsService;

    @Scheduled(fixedRate = Constants.TASK_INTERVAL_MS)
    public void cancelTimeoutOrders() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(Constants.ORDER_TIMEOUT_MINUTES);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderStatus, Constants.ORDER_STATUS_PENDING)
               .eq(Order::getPayStatus, Constants.PAY_STATUS_UNPAID)
               .lt(Order::getCreateTime, timeout);
        List<Order> timeoutOrders = orderMapper.selectList(wrapper);
        for (Order order : timeoutOrders) {
            cancelOrderAndRestoreStock(order);
        }
    }

    @Transactional
    public void cancelOrderAndRestoreStock(Order order) {
        // 取消订单状态
        LambdaUpdateWrapper<Order> cancelWrapper = new LambdaUpdateWrapper<>();
        cancelWrapper.eq(Order::getOrderId, order.getOrderId())
                .notIn(Order::getOrderStatus,
                        Constants.ORDER_STATUS_COMPLETED,
                        Constants.ORDER_STATUS_CANCELLED,
                        Constants.ORDER_STATUS_REFUNDED)
                .set(Order::getOrderStatus, Constants.ORDER_STATUS_CANCELLED);
        orderMapper.update(null, cancelWrapper);

        // 恢复商品库存（原子加法，乐观锁冲突时重试）
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        for (OrderItem item : items) {
            for (int retry = 0; retry < 3; retry++) {
                Food food = foodMapper.selectById(item.getFoodId());
                if (food == null) {
                    break;
                }
                food.setStockQty(food.getStockQty() + item.getQuantity());
                if (foodMapper.updateById(food) > 0) {
                    break;
                }
            }
        }

        // 返还用户抵扣的积分
        Integer pointsUsed = order.getPointsUsed();
        if (pointsUsed != null && pointsUsed > 0) {
            pointsService.earnPoints(order.getUserId(), Constants.SOURCE_POINTS_DEDUCT,
                    pointsUsed, "订单超时取消，积分返还");
        }
    }
}