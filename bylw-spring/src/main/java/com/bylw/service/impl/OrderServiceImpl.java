package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.Constants;
import com.bylw.dto.OrderCreateDTO;
import com.bylw.dto.OrderDTO;
import com.bylw.dto.PageResponse;
import com.bylw.entity.*;
import com.bylw.mapper.*;
import com.bylw.service.OrderService;
import com.bylw.service.DeliveryService;
import com.bylw.service.PointsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private FoodMapper foodMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DeliveryService deliveryService;
    @Autowired
    private PointsService pointsService;

    private static final BigDecimal POINTS_TO_YUAN = new BigDecimal("0.01");

    @Override
    @Transactional
    public OrderDTO create(OrderCreateDTO dto, Integer userId) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单项不能为空");
        }

        List<ValidatedOrderItem> validatedItems = new ArrayList<>();
        Map<Integer, Food> foodCache = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            if (itemDTO == null || itemDTO.getFoodId() == null || itemDTO.getQuantity() == null) {
                throw new IllegalArgumentException("订单项信息不完整");
            }
            if (itemDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("订单商品数量必须大于 0");
            }

            Food food = foodCache.computeIfAbsent(itemDTO.getFoodId(), foodMapper::selectById);
            if (food == null || (food.getDeleted() != null && food.getDeleted() == 1)) {
                throw new IllegalArgumentException("商品不存在: " + itemDTO.getFoodId());
            }
            if (food.getStatus() == null || food.getStatus() != 1) {
                throw new IllegalArgumentException("商品 [" + food.getFoodName() + "] 当前不可下单");
            }
            if (food.getDiscountPrice() == null || food.getDiscountPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("商品 [" + food.getFoodName() + "] 价格异常");
            }

            BigDecimal subtotal = food.getDiscountPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            total = total.add(subtotal);
            // 只存储原始值，避免缓存对象被后续扣减修改导致数据错乱
            validatedItems.add(new ValidatedOrderItem(
                    itemDTO.getFoodId(),
                    food.getFoodName(),
                    food.getDiscountPrice(),
                    itemDTO.getQuantity(),
                    subtotal));
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("订单金额异常，请重新选择商品");
        }

        if (dto.getReceiverName() == null || dto.getReceiverName().trim().isEmpty()) {
            throw new IllegalArgumentException("收货人姓名不能为空");
        }
        if (dto.getReceiverPhone() == null || dto.getReceiverPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("收货人电话不能为空");
        }
        if (dto.getReceiverAddress() == null || dto.getReceiverAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("收货地址不能为空");
        }

        int pointsUsed = 0;
        BigDecimal pointsDeducted = BigDecimal.ZERO;
        Integer pointsToUse = dto.getPointsToUse();
        if (pointsToUse != null && pointsToUse > 0) {
            Integer balance = pointsService.getBalance(userId);
            int usablePoints = Math.min(pointsToUse, balance);
            int maxPointsByAmount = total.multiply(new BigDecimal(Constants.POINTS_PER_YUAN)).setScale(0, RoundingMode.DOWN).intValue();
            pointsUsed = Math.min(usablePoints, maxPointsByAmount);
            pointsDeducted = new BigDecimal(pointsUsed).multiply(POINTS_TO_YUAN).setScale(2, RoundingMode.HALF_UP);
            total = total.subtract(pointsDeducted);
            if (total.compareTo(BigDecimal.ZERO) < 0) {
                total = BigDecimal.ZERO;
            }
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        order.setOrderStatus(Constants.ORDER_STATUS_PENDING);
        order.setPayStatus(Constants.PAY_STATUS_UNPAID);
        order.setCreateTime(LocalDateTime.now());
        order.setDeleted(0);
        order.setTotalAmount(total);
        order.setPointsUsed(pointsUsed);

        orderMapper.insert(order);

        for (ValidatedOrderItem item : validatedItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setFoodId(item.foodId());
            orderItem.setFoodName(item.foodName());
            orderItem.setPrice(item.price());
            orderItem.setQuantity(item.quantity());
            orderItem.setSubtotal(item.subtotal());
            orderItemMapper.insert(orderItem);

            // 原子扣减库存，防止并发超卖
            LambdaUpdateWrapper<Food> stockWrapper = new LambdaUpdateWrapper<>();
            stockWrapper.eq(Food::getFoodId, item.foodId())
                        .ge(Food::getStockQty, item.quantity())
                        .setSql("stock_qty = stock_qty - " + item.quantity());
            int updated = foodMapper.update(new Food(), stockWrapper);
            if (updated == 0) {
                throw new IllegalArgumentException("商品库存不足或并发冲突，请重试");
            }
        }

        return getById(order.getOrderId());
    }

    @Override
    public OrderDTO getById(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) return null;

        OrderDTO dto = toDTO(order);
        fillOrderDetails(dto, orderId);
        return dto;
    }

    @Override
    public PageResponse<OrderDTO> listByUser(Integer userId, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
               .eq(Order::getDeleted, 0)
               .orderByDesc(Order::getCreateTime);

        Page<Order> result = orderMapper.selectPage(page, wrapper);

        // 批量查询订单项（避免 N+1）
        List<Integer> orderIds = result.getRecords().stream()
                .map(Order::getOrderId).collect(Collectors.toList());
        Map<Integer, List<OrderItem>> itemsByOrderId = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);
            for (OrderItem item : allItems) {
                itemsByOrderId.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
            }
        }

        List<OrderDTO> dtoList = new ArrayList<>();
        for (Order order : result.getRecords()) {
            OrderDTO dto = toDTO(order);
            dto.setItems(new ArrayList<>());
            List<OrderItem> items = itemsByOrderId.getOrDefault(order.getOrderId(), List.of());
            for (OrderItem item : items) {
                OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                BeanUtils.copyProperties(item, itemDTO);
                dto.getItems().add(itemDTO);
            }
            Delivery delivery = deliveryService.getByOrderId(order.getOrderId());
            if (delivery != null) {
                OrderDTO.DeliveryDTO deliveryDTO = new OrderDTO.DeliveryDTO();
                BeanUtils.copyProperties(delivery, deliveryDTO);
                dto.setDelivery(deliveryDTO);
            }
            dtoList.add(dto);
        }
        return new PageResponse<>(dtoList, result.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public PageResponse<OrderDTO> listAll(Integer pageNum, Integer pageSize, String status) {
        return listAll(pageNum, pageSize, status, null);
    }

    @Override
    public PageResponse<OrderDTO> listAll(Integer pageNum, Integer pageSize, String status, String keyword) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0)
               .orderByDesc(Order::getCreateTime);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getOrderStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Order::getOrderNo, keyword);
        }

        Page<Order> result = orderMapper.selectPage(page, wrapper);

        // 批量查询用户（避免 N+1）
        List<Integer> orderIds = result.getRecords().stream()
                .map(Order::getOrderId).collect(Collectors.toList());
        Map<Integer, User> userMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<Integer> userIds = result.getRecords().stream()
                    .map(Order::getUserId).distinct().collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getUserId(), u);
            }
        }

        // 批量查询订单项（避免 N+1）
        Map<Integer, List<OrderItem>> itemsByOrderId = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);
            for (OrderItem item : allItems) {
                itemsByOrderId.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
            }
        }

        List<OrderDTO> dtoList = new ArrayList<>();
        for (Order order : result.getRecords()) {
            OrderDTO dto = toDTO(order);
            User user = userMap.get(order.getUserId());
            if (user != null) dto.setUsername(user.getNickname());
            dto.setItems(new ArrayList<>());
            List<OrderItem> items = itemsByOrderId.getOrDefault(order.getOrderId(), List.of());
            for (OrderItem item : items) {
                OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                BeanUtils.copyProperties(item, itemDTO);
                dto.getItems().add(itemDTO);
            }
            dtoList.add(dto);
        }
        return new PageResponse<>(dtoList, result.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    @Transactional
    public boolean updateStatus(Integer orderId, String status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        String current = order.getOrderStatus();
        // 状态机：限制合法的状态流转
        boolean valid = switch (current) {
            case Constants.ORDER_STATUS_PENDING -> false; // 待支付由pay()处理
            case Constants.ORDER_STATUS_PAID -> Constants.ORDER_STATUS_ACCEPTED.equals(status)
                    || Constants.ORDER_STATUS_CANCELLED.equals(status);
            case Constants.ORDER_STATUS_ACCEPTED -> Constants.ORDER_STATUS_DELIVERING.equals(status)
                    || Constants.ORDER_STATUS_CANCELLED.equals(status);
            case Constants.ORDER_STATUS_DELIVERING -> Constants.ORDER_STATUS_COMPLETED.equals(status);
            default -> false; // 已完成/已取消/已退款不可流转
        };
        if (!valid) {
            return false;
        }
        order.setOrderStatus(status);
        orderMapper.updateById(order);

        // 管理员取消订单时，同步关闭配送记录（如果存在）
        if (Constants.ORDER_STATUS_CANCELLED.equals(status)) {
            deliveryService.updateStatus(orderId, Constants.DELIVERY_STATUS_REJECTED);
        }

        // 订单完成时：发放环保积分奖励（减少食品浪费主题亮点）
        if (Constants.ORDER_STATUS_COMPLETED.equals(status)) {
            awardEnvironmentalPoints(order);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean cancel(Integer orderId) {
        // 查询订单信息（获取 pointsUsed 用于积分返还）
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        Integer pointsUsed = order.getPointsUsed();

        // 前置条件检查：骑手已提货（配送中）时，用户无权自行取消，只能申请售后
        Delivery delivery = deliveryService.getByOrderId(orderId);
        if (delivery != null && Constants.DELIVERY_STATUS_DELIVERING.equals(delivery.getDeliveryStatus())) {
            throw new IllegalArgumentException("骑手已提货，请申请售后处理");
        }
        if (delivery != null && Constants.DELIVERY_STATUS_COMPLETED.equals(delivery.getDeliveryStatus())) {
            throw new IllegalArgumentException("订单已完成，请申请售后处理");
        }

        // 使用乐观锁风格的原子更新：WHERE status NOT IN (已完成, 已取消) 保证不重复取消
        LambdaUpdateWrapper<Order> cancelWrapper = new LambdaUpdateWrapper<>();
        cancelWrapper.eq(Order::getOrderId, orderId)
                .notIn(Order::getOrderStatus,
                        Constants.ORDER_STATUS_COMPLETED,
                        Constants.ORDER_STATUS_CANCELLED,
                        Constants.ORDER_STATUS_REFUNDED)
                .set(Order::getOrderStatus, Constants.ORDER_STATUS_CANCELLED);
        int updated = orderMapper.update(null, cancelWrapper);
        if (updated == 0) {
            // 订单不存在或已是终态
            return false;
        }

        // 同步取消配送记录（如果存在且未完成）
        if (delivery != null) {
            deliveryService.updateStatus(orderId, Constants.DELIVERY_STATUS_REJECTED);
        }

        // 恢复每个订单项对应的商品库存
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        for (OrderItem item : items) {
            LambdaUpdateWrapper<Food> stockWrapper = new LambdaUpdateWrapper<>();
            stockWrapper.eq(Food::getFoodId, item.getFoodId())
                    .setSql("stock_qty = stock_qty + " + item.getQuantity());
            foodMapper.update(null, stockWrapper);
        }

        // 返还用户抵扣的积分
        if (pointsUsed != null && pointsUsed > 0) {
            pointsService.earnPoints(order.getUserId(), Constants.SOURCE_POINTS_DEDUCT, pointsUsed,
                    "订单取消，积分返还");
        }

        return true;
    }

    @Override
    @Transactional
    public boolean pay(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        // 只允许对待支付订单发起支付，防止重复支付或对已完成/已取消订单操作
        if (!Constants.ORDER_STATUS_PENDING.equals(order.getOrderStatus())) {
            return false;
        }
        order.setPayStatus(Constants.PAY_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(Constants.ORDER_STATUS_PAID);
        orderMapper.updateById(order);
        return true;
    }

    @Override
    @Transactional
    public boolean acceptOrder(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (!Constants.ORDER_STATUS_PAID.equals(order.getOrderStatus())) {
            return false;
        }
        // 先更新订单状态，确保操作成功后再扣减积分
        order.setOrderStatus(Constants.ORDER_STATUS_ACCEPTED);
        int updated = orderMapper.updateById(order);
        if (updated == 0) {
            return false;
        }
        // 积分扣减在订单状态已更新之后
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            pointsService.spendPoints(order.getUserId(), Constants.SOURCE_POINTS_DEDUCT, order.getPointsUsed());
        }
        deliveryService.createPendingDelivery(orderId);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectOrder(Integer orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (!Constants.ORDER_STATUS_PAID.equals(order.getOrderStatus())) {
            return false;
        }
        // 合并为一次更新：同时设置退款状态和支付状态
        boolean wasPaid = Constants.PAY_STATUS_PAID.equals(order.getPayStatus());
        order.setOrderStatus(Constants.ORDER_STATUS_REFUNDED);
        if (wasPaid) {
            order.setPayStatus(Constants.PAY_STATUS_UNPAID);
        }
        order.setRemark((order.getRemark() == null ? "" : order.getRemark()) + " [拒单原因:" + (reason == null ? "" : reason) + "]");
        orderMapper.updateById(order);
        // 返还用户积分抵扣
        if (wasPaid && order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            pointsService.earnPoints(order.getUserId(), Constants.SOURCE_POINTS_DEDUCT, order.getPointsUsed(), "拒单返还");
        }
        return true;
    }

    @Override
    @Transactional
    public boolean dispatchAssign(Integer orderId, String courierName, String courierPhone) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (!Constants.ORDER_STATUS_ACCEPTED.equals(order.getOrderStatus())
                && !Constants.ORDER_STATUS_DELIVERING.equals(order.getOrderStatus())) {
            return false;
        }
        deliveryService.assignDelivery(orderId, courierName, courierPhone);
        return true;
    }

    @Override
    public List<Order> getRecentOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0)
               .notIn(Order::getOrderStatus,
                       Constants.ORDER_STATUS_COMPLETED,
                       Constants.ORDER_STATUS_CANCELLED,
                       Constants.ORDER_STATUS_REFUNDED)
               .orderByDesc(Order::getCreateTime)
               .last("LIMIT 10");
        return orderMapper.selectList(wrapper);
    }

    @Override
    public boolean deleteOrder(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        order.setDeleted(1);
        orderMapper.updateById(order);
        return true;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(10000);
        return "ORD" + timestamp + String.format("%04d", random);
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        if (dto.getPointsUsed() != null && dto.getPointsUsed() > 0) {
            dto.setPointsDeducted(new BigDecimal(dto.getPointsUsed()).multiply(POINTS_TO_YUAN).setScale(2, RoundingMode.HALF_UP));
        } else {
            dto.setPointsDeducted(BigDecimal.ZERO);
        }
        dto.setItems(new ArrayList<>());
        return dto;
    }

    private void fillOrderDetails(OrderDTO dto, Integer orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
            BeanUtils.copyProperties(item, itemDTO);
            dto.getItems().add(itemDTO);
        }

        Delivery delivery = deliveryService.getByOrderId(orderId);
        if (delivery != null) {
            OrderDTO.DeliveryDTO deliveryDTO = new OrderDTO.DeliveryDTO();
            BeanUtils.copyProperties(delivery, deliveryDTO);
            dto.setDelivery(deliveryDTO);
        }
    }

    /**
     * 环保积分奖励
     * 规则：每完成一单，按购买商品数量奖励积分（每件奖励1分，上限100分/单）
     * 体现"减少临期食品浪费"主题，答辩时可作为亮点功能展示
     */
    private void awardEnvironmentalPoints(Order order) {
        if (order.getUserId() == null) return;

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        int totalQty = items.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();

        // 每件食品奖励1分，单次奖励上限100分
        int rewardPoints = Math.min(totalQty, Constants.ENV_POINTS_PER_ORDER_MAX);
        if (rewardPoints > 0) {
            pointsService.earnPoints(order.getUserId(), Constants.SOURCE_ENV_REWARD,
                    rewardPoints,
                    "环保积分：购买" + totalQty + "件临期食品，减少食品浪费");
        }
    }

    private record ValidatedOrderItem(Integer foodId, String foodName, BigDecimal price, Integer quantity, BigDecimal subtotal) {}
}
