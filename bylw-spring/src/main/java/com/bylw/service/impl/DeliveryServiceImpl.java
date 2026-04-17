package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.common.Constants;
import com.bylw.entity.Courier;
import com.bylw.entity.Delivery;
import com.bylw.entity.Order;
import com.bylw.mapper.CourierMapper;
import com.bylw.mapper.DeliveryMapper;
import com.bylw.mapper.OrderMapper;
import com.bylw.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryMapper deliveryMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CourierMapper courierMapper;

    @Override
    public Delivery getByOrderId(Integer orderId) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Delivery::getOrderId, orderId);
        return deliveryMapper.selectOne(wrapper);
    }

    @Override
    public Delivery assignDelivery(Integer orderId, String courierName, String courierPhone) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Delivery::getOrderId, orderId);
        Delivery delivery = deliveryMapper.selectOne(wrapper);
        if (delivery != null) {
            delivery.setCourierName(courierName);
            delivery.setCourierPhone(courierPhone);
            delivery.setDeliveryStatus(Constants.DELIVERY_STATUS_ACCEPTED);
            delivery.setDispatchTime(LocalDateTime.now());
            deliveryMapper.updateById(delivery);
            return delivery;
        }
        Delivery newDelivery = new Delivery();
        newDelivery.setOrderId(orderId);
        newDelivery.setCourierName(courierName);
        newDelivery.setCourierPhone(courierPhone);
        newDelivery.setDeliveryStatus(Constants.DELIVERY_STATUS_ACCEPTED);
        newDelivery.setDispatchTime(LocalDateTime.now());
        deliveryMapper.insert(newDelivery);
        return newDelivery;
    }

    @Override
    public Delivery createPendingDelivery(Integer orderId) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Delivery::getOrderId, orderId);
        Delivery existing = deliveryMapper.selectOne(wrapper);
        if (existing != null) {
            return existing;
        }
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setDeliveryStatus(Constants.DELIVERY_STATUS_PENDING);
        deliveryMapper.insert(delivery);
        return delivery;
    }

    @Override
    public boolean updateStatus(Integer orderId, String status) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Delivery::getOrderId, orderId);
        Delivery delivery = deliveryMapper.selectOne(wrapper);
        if (delivery != null) {
            delivery.setDeliveryStatus(status);
            if (Constants.DELIVERY_STATUS_COMPLETED.equals(status)) {
                delivery.setFinishTime(LocalDateTime.now());
            }
            deliveryMapper.updateById(delivery);
            return true;
        }
        return false;
    }

    @Override
    public List<Order> getOrdersByCourier(String courierPhone, String courierName, String status) {
        LambdaQueryWrapper<Delivery> dWrapper = new LambdaQueryWrapper<>();
        boolean hasPhone = courierPhone != null && !courierPhone.isEmpty();
        boolean hasName = courierName != null && !courierName.isEmpty();
        if (!hasPhone && !hasName) {
            return new ArrayList<>();
        }
        if (hasPhone) {
            dWrapper.eq(Delivery::getCourierPhone, courierPhone);
        }
        if (hasName) {
            dWrapper.eq(Delivery::getCourierName, courierName);
        }
        if (status != null && !status.isEmpty()) {
            dWrapper.eq(Delivery::getDeliveryStatus, status);
        }
        List<Delivery> deliveries = deliveryMapper.selectList(dWrapper);
        if (deliveries.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> orderIds = deliveries.stream().map(Delivery::getOrderId).distinct().toList();
        LambdaQueryWrapper<Order> oWrapper = new LambdaQueryWrapper<>();
        oWrapper.in(Order::getOrderId, orderIds);
        oWrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(oWrapper);
    }

    @Override
    public List<Order> getHallOrders(String phone, String courierName) {
        boolean hasPhone = phone != null && !phone.isEmpty();
        boolean hasName = courierName != null && !courierName.isEmpty();
        log.info("[getHallOrders] phone={}, hasPhone={}, hasName={}", phone, hasPhone, hasName);
        Set<Integer> orderIdSet = new HashSet<>();
        // Truly unclaimed orders (no courier assigned)
        LambdaQueryWrapper<Delivery> unclaimedWrapper = new LambdaQueryWrapper<>();
        unclaimedWrapper.eq(Delivery::getDeliveryStatus, Constants.DELIVERY_STATUS_PENDING)
                       .isNull(Delivery::getCourierName);
        List<Delivery> unclaimed = deliveryMapper.selectList(unclaimedWrapper);
        log.info("[getHallOrders] unclaimed count: {}", unclaimed.size());
        unclaimed.forEach(d -> orderIdSet.add(d.getOrderId()));
        // Pre-assigned to this rider (by phone OR by name)
        if (hasPhone || hasName) {
            LambdaQueryWrapper<Delivery> preWrapper = new LambdaQueryWrapper<>();
            if (hasPhone && hasName) {
                preWrapper.eq(Delivery::getDeliveryStatus, Constants.DELIVERY_STATUS_PENDING)
                         .and(w -> w.eq(Delivery::getCourierPhone, phone).or().eq(Delivery::getCourierName, courierName));
            } else if (hasPhone) {
                preWrapper.eq(Delivery::getDeliveryStatus, Constants.DELIVERY_STATUS_PENDING)
                         .eq(Delivery::getCourierPhone, phone);
            } else {
                preWrapper.eq(Delivery::getDeliveryStatus, Constants.DELIVERY_STATUS_PENDING)
                         .eq(Delivery::getCourierName, courierName);
            }
            List<Delivery> preAssigned = deliveryMapper.selectList(preWrapper);
            log.info("[getHallOrders] preAssigned count: {}", preAssigned.size());
            preAssigned.forEach(d -> orderIdSet.add(d.getOrderId()));
        }
        log.info("[getHallOrders] total unique orderIds: {}", orderIdSet.size());
        if (orderIdSet.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Order> oWrapper = new LambdaQueryWrapper<>();
        oWrapper.in(Order::getOrderId, orderIdSet);
        oWrapper.eq(Order::getOrderStatus, Constants.ORDER_STATUS_ACCEPTED);
        List<Order> result = orderMapper.selectList(oWrapper);
        log.info("[getHallOrders] orders with orderStatus=ACCEPTED: {}", result.size());
        return result;
    }

    @Override
    public Courier getCourierByName(String courierName) {
        if (courierName == null || courierName.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Courier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Courier::getCourierName, courierName);
        return courierMapper.selectOne(wrapper);
    }
}
