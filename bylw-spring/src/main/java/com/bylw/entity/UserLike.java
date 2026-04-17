package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_like")
public class UserLike {

    @TableId(type = IdType.AUTO)
    private Integer likeId;

    private Integer userId;

    private String targetType;

    private Integer targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}