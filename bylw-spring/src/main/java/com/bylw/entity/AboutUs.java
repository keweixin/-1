package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_about_us")
public class AboutUs {

    @TableId(type = IdType.AUTO)
    private Integer aboutId;

    private String platformName;
    private String phone;
    private String email;
    private String address;
    private String introText;
    private String coverImg;
    private LocalDateTime updateTime;
}
