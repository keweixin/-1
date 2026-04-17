package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior {

    @TableId(type = IdType.AUTO)
    private Integer behaviorId;

    private Integer userId;
    private String targetType;
    private Integer targetId;
    private String behaviorType;
    private BigDecimal weightScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
