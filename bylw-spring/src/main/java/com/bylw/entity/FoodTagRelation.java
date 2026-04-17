package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("food_tag_relation")
public class FoodTagRelation {

    @TableId(type = IdType.AUTO)
    private Integer relationId;

    private Integer foodId;
    private Integer tagId;
    private LocalDateTime updateTime;
}
