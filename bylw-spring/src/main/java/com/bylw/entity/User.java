package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer userId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度需在2-20之间")
    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private String gender;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;
    private String address;
    private String role;
    private Integer roleType;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
