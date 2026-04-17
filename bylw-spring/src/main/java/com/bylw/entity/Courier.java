package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("courier")
public class Courier {

    @TableId(type = IdType.AUTO)
    private Integer courierId;

    private Integer userId;
    private String courierName;
    private String courierPhone;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
