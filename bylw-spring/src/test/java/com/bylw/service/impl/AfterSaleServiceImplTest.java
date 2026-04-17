package com.bylw.service.impl;

import com.bylw.common.Constants;
import com.bylw.entity.AfterSale;
import com.bylw.entity.Order;
import com.bylw.mapper.OrderItemMapper;
import com.bylw.mapper.OrderMapper;
import com.bylw.mapper.FoodMapper;
import com.bylw.mapper.AfterSaleMapper;
import com.bylw.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AfterSaleServiceImplTest {

    @Mock
    private AfterSaleMapper afterSaleMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private FoodMapper foodMapper;
    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private AfterSaleServiceImpl afterSaleService;

    @Test
    void handleShouldPersistApprovedStatusWhenNotProvided() {
        AfterSale afterSale = new AfterSale();
        afterSale.setAfterSaleId(1);
        afterSale.setOrderId(100);
        afterSale.setHandleStatus(Constants.HANDLE_STATUS_PENDING);
        when(afterSaleMapper.selectById(1)).thenReturn(afterSale);

        Order order = new Order();
        order.setOrderId(100);
        when(orderMapper.selectById(100)).thenReturn(order);

        boolean success = afterSaleService.handle(1, null, "同意退款");

        assertThat(success).isTrue();
        assertThat(afterSale.getHandleStatus()).isEqualTo(Constants.HANDLE_STATUS_APPROVED);
        assertThat(afterSale.getHandleResult()).isEqualTo("同意退款");
        assertThat(order.getOrderStatus()).isEqualTo(Constants.ORDER_STATUS_REFUNDED);
    }

    @Test
    void handleShouldPersistRejectedStatusWhenProvided() {
        AfterSale afterSale = new AfterSale();
        afterSale.setAfterSaleId(2);
        afterSale.setHandleStatus(Constants.HANDLE_STATUS_PENDING);
        when(afterSaleMapper.selectById(2)).thenReturn(afterSale);

        boolean success = afterSaleService.handle(2, Constants.HANDLE_STATUS_REJECTED, "证据不足");

        assertThat(success).isTrue();
        assertThat(afterSale.getHandleStatus()).isEqualTo(Constants.HANDLE_STATUS_REJECTED);
        assertThat(afterSale.getHandleResult()).isEqualTo("证据不足");
    }
}
