package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("food_profile")
public class FoodProfile {

    @TableId(type = IdType.AUTO)
    private Integer profileId;

    private Integer userId;
    private String tastePreference;
    private String allergenInfo;
    private String chronicDisease;
    private String tabooInfo;
    private String remark;

    private LocalDateTime updateTime;
}
