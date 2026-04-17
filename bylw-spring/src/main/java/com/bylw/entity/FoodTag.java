package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("food_tag")
public class FoodTag {

    @TableId(type = IdType.AUTO)
    private Integer tagId;

    private String tagName;
    private String tagType;
    private String description;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
