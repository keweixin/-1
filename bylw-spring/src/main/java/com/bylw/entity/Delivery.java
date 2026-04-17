package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("food_delivery")
public class Delivery {

    @TableId(type = IdType.AUTO)
    private Integer deliveryId;

    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    private String courierName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String courierPhone;

    private String deliveryStatus;
    private LocalDateTime dispatchTime;
    private LocalDateTime finishTime;
    private String remark;
    private LocalDateTime updateTime;
}
