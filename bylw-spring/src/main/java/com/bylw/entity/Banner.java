package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_banner")
public class Banner {

    @TableId(type = IdType.AUTO)
    private Integer bannerId;

    private String title;
    private String imgUrl;
    private String linkUrl;
    private Integer sortNo;
    private Integer status;
}
