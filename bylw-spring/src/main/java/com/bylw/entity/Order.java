package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("food_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Integer orderId;

    private String orderNo;

    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    private BigDecimal totalAmount;
    private String orderStatus;
    private String payStatus;

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime payTime;

    @Min(value = 0, message = "积分抵扣不能为负数")
    private Integer pointsUsed;
}
