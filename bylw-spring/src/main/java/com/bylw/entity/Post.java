package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community_post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Integer postId;

    private Integer userId;
    private String title;
    private String content;
    private String imgList;
    private Integer likeCount;
    private Integer commentCount;
    private String auditStatus;

    private Integer recommended;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
