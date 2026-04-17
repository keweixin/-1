package com.bylw.service;

import com.bylw.dto.OrderDTO;
import com.bylw.dto.OrderCreateDTO;
import com.bylw.dto.PageResponse;
import com.bylw.entity.Order;

import java.util.List;

public interface OrderService {
    OrderDTO create(OrderCreateDTO dto, Integer userId);
    OrderDTO getById(Integer orderId);
    PageResponse<OrderDTO> listByUser(Integer userId, Integer pageNum, Integer pageSize);
    PageResponse<OrderDTO> listAll(Integer pageNum, Integer pageSize, String status);
    boolean updateStatus(Integer orderId, String status);
    boolean cancel(Integer orderId);
    boolean pay(Integer orderId);
    boolean acceptOrder(Integer orderId);
    boolean rejectOrder(Integer orderId, String reason);
    boolean dispatchAssign(Integer orderId, String courierName, String courierPhone);
    List<Order> getRecentOrders();
}
