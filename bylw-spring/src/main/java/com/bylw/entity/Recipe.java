package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("food_recipe")
public class Recipe {

    @TableId(type = IdType.AUTO)
    private Integer recipeId;

    private String title;
    private String summary;
    private String content;
    private String suitablePeople;
    private String coverImg;
    private Integer publisherId;
    private Integer collectCount;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime publishTime;
}
