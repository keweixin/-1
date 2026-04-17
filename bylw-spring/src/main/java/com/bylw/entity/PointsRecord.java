package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Integer recordId;

    private Integer userId;
    private String changeType;
    private Integer changeValue;
    private String sourceType;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
