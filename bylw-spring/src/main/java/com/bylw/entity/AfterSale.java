package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("food_after_sale")
public class AfterSale {

    @TableId(type = IdType.AUTO)
    private Integer afterSaleId;

    private Integer orderId;
    private Integer userId;
    private String reasonType;
    private String reasonDesc;
    private String evidenceImg;
    private String handleStatus;
    private String handleResult;
    private LocalDateTime applyTime;
    private LocalDateTime handleTime;
    private LocalDateTime updateTime;
}
