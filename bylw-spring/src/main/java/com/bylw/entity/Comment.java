package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Integer commentId;

    private Integer postId;
    private Integer userId;
    private String content;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
