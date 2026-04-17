package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recommendation")
public class Recommendation {

    @TableId(type = IdType.AUTO)
    private Integer recId;

    private Integer userId;
    private String targetType;
    private Integer targetId;
    private BigDecimal scoreValue;
    private String recReason;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime createTime;
}
