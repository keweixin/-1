package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_notice")
public class Notice {

    @TableId(type = IdType.AUTO)
    private Integer noticeId;

    private String title;
    private String content;
    private Integer publisherId;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime publishTime;
}
