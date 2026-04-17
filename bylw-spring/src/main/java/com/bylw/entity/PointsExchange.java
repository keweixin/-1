package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("points_exchange")
public class PointsExchange {

    @TableId(type = IdType.AUTO)
    private Integer exchangeId;

    private Integer goodsId;
    private Integer userId;
    private String exchangeStatus;
    private LocalDateTime createTime;
}
