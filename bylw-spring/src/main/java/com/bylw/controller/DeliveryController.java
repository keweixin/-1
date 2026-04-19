package com.bylw.controller;

import com.bylw.common.AuthUtil;
import com.bylw.common.Constants;
import com.bylw.common.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.entity.Order;
import com.bylw.entity.Delivery;
import com.bylw.entity.User;
import com.bylw.entity.Courier;
import com.bylw.mapper.DeliveryMapper;
import com.bylw.mapper.OrderMapper;
import com.bylw.mapper.UserMapper;
import com.bylw.mapper.CourierMapper;
import com.bylw.service.DeliveryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DeliveryMapper deliveryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CourierMapper courierMapper;

    @GetMapping("/order/{orderId}")
    public Result<?> getByOrderId(HttpServletRequest request, @PathVariable Integer orderId) {
        Integer userId = authUtil.getUserId(request);
        if (!authUtil.isAdmin(request)) {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("订单不存在");
            }
            if (!userId.equals(order.getUserId())) {
                throw new IllegalArgumentException("无权查看该配送信息");
            }
        }
        return Result.success(deliveryService.getByOrderId(orderId));
    }

    @PostMapping("/assign")
    public Result<?> assign(HttpServletRequest request,
                           @RequestParam Integer orderId,
                           @RequestParam String courierName,
                           @RequestParam String courierPhone) {
        authUtil.verifyAdmin(request);
        return Result.success(deliveryService.assignDelivery(orderId, courierName, courierPhone));
    }

    @PutMapping("/status/{orderId}")
    public Result<?> updateStatus(HttpServletRequest request, @PathVariable Integer orderId, @RequestParam String status) {
        authUtil.verifyAdmin(request);
        return Result.success(deliveryService.updateStatus(orderId, status));
    }

    @GetMapping("/courier/tasks")
    public Result<?> getCourierTasks(HttpServletRequest request,
                                      @RequestParam(required = false) String status) {
        if (!authUtil.isCourier(request) && !authUtil.isAdmin(request)) {
            throw new IllegalArgumentException("权限不足");
        }
        String phone = authUtil.getPhone(request);
        String courierName = authUtil.getNickname(request);
        if (phone == null && courierName == null) {
            return Result.success(List.of());
        }
        List<Order> orders = deliveryService.getOrdersByCourier(phone, courierName, status);
        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("orderNo", order.getOrderNo());
            map.put("orderStatus", order.getOrderStatus());
            map.put("payStatus", order.getPayStatus());
            map.put("receiverName", order.getReceiverName());
            map.put("receiverPhone", order.getReceiverPhone());
            map.put("receiverAddress", order.getReceiverAddress());
            map.put("totalAmount", order.getTotalAmount());
            map.put("createTime", order.getCreateTime());
            Delivery delivery = deliveryService.getByOrderId(order.getOrderId());
            map.put("courierName", delivery != null ? delivery.getCourierName() : null);
            map.put("courierPhone", delivery != null ? delivery.getCourierPhone() : null);
            map.put("deliveryStatus", delivery != null ? delivery.getDeliveryStatus() : null);
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/hall")
    public Result<?> getHallOrders(HttpServletRequest request) {
        if (!authUtil.isCourier(request) && !authUtil.isAdmin(request)) {
            throw new IllegalArgumentException("权限不足");
        }
        String phone = authUtil.getPhone(request);
        String courierName = authUtil.getNickname(request);
        log.info("[Hall] phone={}, courierName={}", phone, courierName);
        List<Order> orders = deliveryService.getHallOrders(phone, courierName);
        log.info("[Hall] orders found: {}", orders.size());
        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("orderNo", order.getOrderNo());
            map.put("receiverName", order.getReceiverName());
            map.put("receiverPhone", order.getReceiverPhone());
            map.put("receiverAddress", order.getReceiverAddress());
            map.put("totalAmount", order.getTotalAmount());
            map.put("createTime", order.getCreateTime());
            Delivery delivery = deliveryService.getByOrderId(order.getOrderId());
            map.put("deliveryStatus", delivery != null ? delivery.getDeliveryStatus() : null);
            map.put("preAssigned", delivery != null && delivery.getCourierName() != null);
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/courier/profile")
    public Result<?> getCourierProfile(HttpServletRequest request) {
        if (!authUtil.isCourier(request)) {
            throw new IllegalArgumentException("权限不足");
        }
        String phone = authUtil.getPhone(request);
        String nickname = authUtil.getNickname(request);
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("nickname", nickname);
        profile.put("phone", phone);
        if (nickname != null && !nickname.isEmpty()) {
            var courier = deliveryService.getCourierByName(nickname);
            if (courier != null) {
                profile.put("courierStatus", courier.getStatus());
            }
        }
        return Result.success(profile);
    }

    @PostMapping("/claim/{orderId}")
    public Result<?> claimOrder(HttpServletRequest request, @PathVariable Integer orderId) {
        log.info("[Claim] orderId={}", orderId);
        if (!authUtil.isCourier(request)) {
            throw new IllegalArgumentException("权限不足");
        }
        String phone = authUtil.getPhone(request);
        String courierName = authUtil.getNickname(request);
        log.info("[Claim] phone={}, courierName={}", phone, courierName);
        if (phone == null && courierName == null) {
            throw new IllegalArgumentException("骑手信息不完整");
        }
        Delivery delivery = deliveryService.getByOrderId(orderId);
        log.info("[Claim] delivery={}", delivery);
        if (delivery == null) {
            throw new IllegalArgumentException("该订单没有配送记录");
        }
        if (!Constants.DELIVERY_STATUS_PENDING.equals(delivery.getDeliveryStatus())) {
            throw new IllegalArgumentException("该订单已被其他骑手接单");
        }
        delivery.setCourierName(courierName);
        delivery.setCourierPhone(phone);
        delivery.setDeliveryStatus(Constants.DELIVERY_STATUS_ACCEPTED);
        delivery.setDispatchTime(LocalDateTime.now());
        deliveryMapper.updateById(delivery);
        log.info("[Claim] success for orderId={}", orderId);
        return Result.success(true);
    }

    @PutMapping("/courier/status/{orderId}")
    public Result<?> updateCourierStatus(HttpServletRequest request,
                                         @PathVariable Integer orderId,
                                         @RequestParam String status) {
        if (!authUtil.isCourier(request)) {
            throw new IllegalArgumentException("权限不足");
        }
        String phone = authUtil.getPhone(request);
        String courierName = authUtil.getNickname(request);
        Delivery delivery = deliveryService.getByOrderId(orderId);
        if (delivery == null) {
            throw new IllegalArgumentException("该订单没有配送记录");
        }
        if (courierName != null && !courierName.equals(delivery.getCourierName())) {
            throw new IllegalArgumentException("该订单不是您的配送任务");
        }
        if (phone != null && !phone.equals(delivery.getCourierPhone())) {
            throw new IllegalArgumentException("该订单不是您的配送任务");
        }
        if (Constants.DELIVERY_STATUS_COMPLETED.equals(status)) {
            Order order = orderMapper.selectById(orderId);
            if (order != null && (Constants.ORDER_STATUS_DELIVERING.equals(order.getOrderStatus())
                    || Constants.ORDER_STATUS_ACCEPTED.equals(order.getOrderStatus()))) {
                order.setOrderStatus(Constants.ORDER_STATUS_COMPLETED);
                orderMapper.updateById(order);
            }
        }
        if (Constants.DELIVERY_STATUS_DELIVERING.equals(status)) {
            Order order = orderMapper.selectById(orderId);
            if (order != null && Constants.ORDER_STATUS_ACCEPTED.equals(order.getOrderStatus())) {
                order.setOrderStatus(Constants.ORDER_STATUS_DELIVERING);
                orderMapper.updateById(order);
            }
        }
        return Result.success(deliveryService.updateStatus(orderId, status));
    }

    @PutMapping("/courier/online-status")
    public Result<?> updateOnlineStatus(HttpServletRequest request, @RequestParam Integer status) {
        if (!authUtil.isCourier(request)) {
            throw new IllegalArgumentException("权限不足");
        }
        Integer userId = authUtil.getUserId(request);
        LambdaQueryWrapper<Courier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Courier::getUserId, userId);
        Courier courier = courierMapper.selectOne(wrapper);
        if (courier == null) {
            courier = new Courier();
            courier.setUserId(userId);
            courier.setCourierName(authUtil.getNickname(request));
            courier.setCourierPhone(authUtil.getPhone(request));
            courier.setStatus(status);
            courierMapper.insert(courier);
        } else {
            courier.setStatus(status);
            courierMapper.updateById(courier);
        }
        return Result.success(true);
    }
}
