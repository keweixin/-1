package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_friend_link")
public class FriendLink {

    @TableId(type = IdType.AUTO)
    private Integer linkId;

    private String linkName;
    private String linkUrl;
    private Integer sortNo;
    private Integer status;
}
