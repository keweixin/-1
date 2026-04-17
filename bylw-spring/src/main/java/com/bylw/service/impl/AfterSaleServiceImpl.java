package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.Constants;
import com.bylw.entity.AfterSale;
import com.bylw.entity.Delivery;
import com.bylw.entity.Food;
import com.bylw.entity.Order;
import com.bylw.entity.OrderItem;
import com.bylw.entity.PointsRecord;
import com.bylw.mapper.AfterSaleMapper;
import com.bylw.mapper.DeliveryMapper;
import com.bylw.mapper.FoodMapper;
import com.bylw.mapper.OrderItemMapper;
import com.bylw.mapper.OrderMapper;
import com.bylw.mapper.PointsRecordMapper;
import com.bylw.service.AfterSaleService;
import com.bylw.service.DeliveryService;
import com.bylw.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AfterSaleServiceImpl implements AfterSaleService {

    @Autowired
    private AfterSaleMapper afterSaleMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private FoodMapper foodMapper;
    @Autowired
    private DeliveryService deliveryService;
    @Autowired
    private DeliveryMapper deliveryMapper;
    @Autowired
    private PointsService pointsService;
    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Override
    public AfterSale apply(AfterSale afterSale) {
        if (afterSale.getOrderId() == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        Order order = orderMapper.selectById(afterSale.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getUserId().equals(afterSale.getUserId())) {
            throw new IllegalArgumentException("无权为该订单申请售后");
        }
        if (Constants.ORDER_STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("待支付订单不能申请售后");
        }
        if (Constants.ORDER_STATUS_CANCELLED.equals(order.getOrderStatus())
                || Constants.ORDER_STATUS_REFUNDED.equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("当前订单状态不支持申请售后");
        }

        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSale::getOrderId, afterSale.getOrderId())
                .and(q -> q.eq(AfterSale::getHandleStatus, Constants.HANDLE_STATUS_PENDING)
                        .or()
                        .eq(AfterSale::getHandleStatus, Constants.HANDLE_STATUS_APPROVED));
        if (afterSaleMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("该订单已有进行中的售后申请");
        }

        afterSale.setHandleStatus(Constants.HANDLE_STATUS_PENDING);
        afterSale.setApplyTime(LocalDateTime.now());
        afterSaleMapper.insert(afterSale);
        return afterSale;
    }

    @Override
    public AfterSale getById(Integer afterSaleId) {
        return afterSaleMapper.selectById(afterSaleId);
    }

    @Override
    public Page<AfterSale> listAll(Integer pageNum, Integer pageSize, String status) {
        Page<AfterSale> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AfterSale::getApplyTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AfterSale::getHandleStatus, status);
        }
        return afterSaleMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<AfterSale> listByUser(Integer userId, Integer pageNum, Integer pageSize) {
        Page<AfterSale> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSale::getUserId, userId)
               .orderByDesc(AfterSale::getApplyTime);
        return afterSaleMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean handle(Integer afterSaleId, String handleStatus, String handleResult) {
        AfterSale afterSale = afterSaleMapper.selectById(afterSaleId);
        if (afterSale == null) {
            return false;
        }

        if (!Constants.HANDLE_STATUS_PENDING.equals(afterSale.getHandleStatus())) {
            throw new IllegalArgumentException("该售后申请已处理，请勿重复操作");
        }

        String finalStatus = (handleStatus != null && !handleStatus.isEmpty())
                ? handleStatus
                : Constants.HANDLE_STATUS_APPROVED;
        if (!Constants.HANDLE_STATUS_APPROVED.equals(finalStatus)
                && !Constants.HANDLE_STATUS_REJECTED.equals(finalStatus)) {
            throw new IllegalArgumentException("售后处理状态无效");
        }

        afterSale.setHandleStatus(finalStatus);
        afterSale.setHandleResult(handleResult);
        afterSale.setHandleTime(LocalDateTime.now());

        if (Constants.HANDLE_STATUS_APPROVED.equals(finalStatus)) {
            Order order = orderMapper.selectById(afterSale.getOrderId());
            if (order == null) {
                throw new IllegalArgumentException("关联订单不存在");
            }
            if (Constants.ORDER_STATUS_REFUNDED.equals(order.getOrderStatus())) {
                throw new IllegalArgumentException("该订单已退款，请勿重复处理");
            }
            // 退款前置条件：骑手已提货（配送中）时不允许通过售后退款，需先协调骑手完成或取消配送
            Delivery delivery = deliveryService.getByOrderId(afterSale.getOrderId());
            if (delivery != null && Constants.DELIVERY_STATUS_DELIVERING.equals(delivery.getDeliveryStatus())) {
                throw new IllegalArgumentException("骑手正在配送中，请先协调骑手完成或取消配送后再申请退款");
            }
            restoreStock(afterSale.getOrderId());
            // 退款时返还用户积分抵扣
            Integer pointsUsed = order.getPointsUsed();
            if (pointsUsed != null && pointsUsed > 0) {
                pointsService.earnPoints(order.getUserId(), Constants.SOURCE_POINTS_DEDUCT,
                        pointsUsed, "订单退款，积分返还");
            }
            order.setOrderStatus(Constants.ORDER_STATUS_REFUNDED);
            order.setPayStatus("已退款");
            orderMapper.updateById(order);
            // 同步关闭配送记录（如果存在）
            if (delivery != null) {
                deliveryService.updateStatus(afterSale.getOrderId(), Constants.DELIVERY_STATUS_REJECTED);
            }
        }

        afterSaleMapper.updateById(afterSale);
        return true;
    }

    private void restoreStock(Integer orderId) {
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        for (OrderItem item : items) {
            // 每次重试最多3次，乐观锁冲突时自动重试
            for (int retry = 0; retry < 3; retry++) {
                Food food = foodMapper.selectById(item.getFoodId());
                if (food == null) {
                    break;
                }
                food.setStockQty(food.getStockQty() + item.getQuantity());
                if (foodMapper.updateById(food) > 0) {
                    break;
                }
                // 乐观锁冲突，重试
            }
        }
    }
}
