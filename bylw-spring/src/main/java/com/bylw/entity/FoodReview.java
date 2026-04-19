package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("food_review")
public class FoodReview {

    @TableId(type = IdType.AUTO)
    private Integer reviewId;

    private Integer foodId;
    private Integer userId;
    private String content;
    private Integer rating;
    private Integer deleted;
    private LocalDateTime createTime;
}
