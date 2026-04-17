package com.bylw.dto;

import lombok.Data;

/**
 * 注册接口响应 DTO，排除密码字段防止泄露
 */
@Data
public class UserResponse {
    private Integer userId;
    private String username;
    private String nickname;
    private String gender;
    private String phone;
    private String email;
    private String avatar;
    private String address;
    private String role;
    private Integer roleType;
    private String createTime;

    public static UserResponse from(com.bylw.entity.User user) {
        UserResponse r = new UserResponse();
        r.setUserId(user.getUserId());
        r.setUsername(user.getUsername());
        r.setNickname(user.getNickname());
        r.setGender(user.getGender());
        r.setPhone(user.getPhone());
        r.setEmail(user.getEmail());
        r.setAvatar(user.getAvatar());
        r.setAddress(user.getAddress());
        r.setRole(user.getRole());
        r.setRoleType(user.getRoleType());
        r.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        return r;
    }
}
