package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("food")
public class Food {

    @TableId(type = IdType.AUTO)
    private Integer foodId;

    private Integer merchantId;

    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;

    @NotBlank(message = "食品名称不能为空")
    @Size(max = 100, message = "食品名称不能超过100个字符")
    private String foodName;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal originPrice;

    @DecimalMin(value = "0.01", message = "折扣价必须大于0")
    private BigDecimal discountPrice;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stockQty;

    @Future(message = "过期日期必须在当前时间之后")
    private LocalDateTime expireDate;
    private String nutritionDesc;
    private String suitablePeople;
    private String allergenInfo;
    private String tasteDesc;
    private String coverImg;
    private String description;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @Version
    private Integer version;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
