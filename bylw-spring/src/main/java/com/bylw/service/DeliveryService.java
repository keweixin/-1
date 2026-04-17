package com.bylw.service;

import com.bylw.entity.Courier;
import com.bylw.entity.Delivery;
import com.bylw.entity.Order;

import java.util.List;

public interface DeliveryService {
    Delivery getByOrderId(Integer orderId);
    Delivery assignDelivery(Integer orderId, String courierName, String courierPhone);
    Delivery createPendingDelivery(Integer orderId);
    boolean updateStatus(Integer orderId, String status);
    List<Order> getOrdersByCourier(String courierPhone, String courierName, String status);
    List<Order> getHallOrders(String phone, String courierName);
    Courier getCourierByName(String courierName);
}
