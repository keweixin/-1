package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("food_order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Integer itemId;

    private Integer orderId;
    private Integer foodId;
    private String foodName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
