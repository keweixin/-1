package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("points_goods")
public class PointsGoods {

    @TableId(type = IdType.AUTO)
    private Integer goodsId;

    private String goodsName;
    private Integer pointCost;
    private Integer stockQty;
    private String coverImg;
    private String description;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @Version
    private Integer version;
}
