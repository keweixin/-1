package com.bylw.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Integer orderId;
    private String orderNo;
    private Integer userId;
    private String username;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String payStatus;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private Integer pointsUsed;
    private BigDecimal pointsDeducted;
    private List<OrderItemDTO> items;
    private DeliveryDTO delivery;

    @Data
    public static class OrderItemDTO {
        private Integer itemId;
        private Integer foodId;
        private String foodName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }

    @Data
    public static class DeliveryDTO {
        private Integer deliveryId;
        private String courierName;
        private String courierPhone;
        private String deliveryStatus;
        private LocalDateTime dispatchTime;
        private LocalDateTime finishTime;
    }
}
