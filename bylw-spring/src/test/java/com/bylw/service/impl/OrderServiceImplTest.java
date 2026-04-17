package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.dto.OrderDTO;
import com.bylw.dto.PageResponse;
import com.bylw.entity.Delivery;
import com.bylw.entity.Order;
import com.bylw.entity.OrderItem;
import com.bylw.mapper.DeliveryMapper;
import com.bylw.mapper.FoodMapper;
import com.bylw.mapper.OrderItemMapper;
import com.bylw.mapper.OrderMapper;
import com.bylw.mapper.UserMapper;
import com.bylw.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private FoodMapper foodMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private DeliveryService deliveryService;
    @Mock
    private DeliveryMapper deliveryMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void listByUserShouldIncludeItemsAndDelivery() {
        Order order = new Order();
        order.setOrderId(1);
        order.setOrderNo("ORD20260406120000");
        order.setUserId(99);
        order.setTotalAmount(new BigDecimal("32.50"));

        Page<Order> orderPage = new Page<>(1, 10);
        orderPage.setTotal(1);
        orderPage.setRecords(List.of(order));
        when(orderMapper.selectPage(any(), any())).thenReturn(orderPage);

        OrderItem item = new OrderItem();
        item.setItemId(11);
        item.setOrderId(1);
        item.setFoodId(1001);
        item.setFoodName("测试菜品");
        item.setPrice(new BigDecimal("16.25"));
        item.setQuantity(2);
        item.setSubtotal(new BigDecimal("32.50"));
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(88);
        delivery.setOrderId(1);
        delivery.setCourierName("张三");
        delivery.setCourierPhone("13800000000");
        delivery.setDeliveryStatus("配送中");
        when(deliveryService.getByOrderId(1)).thenReturn(delivery);

        PageResponse<OrderDTO> result = orderService.listByUser(99, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        OrderDTO dto = result.getRecords().get(0);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getFoodName()).isEqualTo("测试菜品");
        assertThat(dto.getDelivery()).isNotNull();
        assertThat(dto.getDelivery().getCourierName()).isEqualTo("张三");

        verify(orderMapper).selectPage(any(), any());
        verify(orderItemMapper).selectList(any(LambdaQueryWrapper.class));
        verify(deliveryService).getByOrderId(1);
    }
}
