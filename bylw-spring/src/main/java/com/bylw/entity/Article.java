package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("food_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Integer articleId;

    private String title;
    private String summary;
    private String content;
    private String coverImg;
    private Integer publisherId;
    private Integer readCount;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime publishTime;
}
