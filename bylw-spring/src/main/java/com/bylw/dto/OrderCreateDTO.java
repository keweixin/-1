package com.bylw.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDTO {
    private Integer userId; // 从 JWT 提取，不再从 DTO 读取

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "联系电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    private String remark;

    private Integer pointsToUse;

    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "食品ID不能为空")
        private Integer foodId;

        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须为正数")
        private Integer quantity;

        private BigDecimal price;
        private String foodName;
        private BigDecimal subtotal;
    }
}
