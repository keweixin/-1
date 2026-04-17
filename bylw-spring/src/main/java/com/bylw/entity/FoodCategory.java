package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("food_category")
public class FoodCategory {

    @TableId(type = IdType.AUTO)
    private Integer categoryId;

    private String categoryName;
    private String description;
    private Integer sortNo;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
