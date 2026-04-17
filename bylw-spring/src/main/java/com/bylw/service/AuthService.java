package com.bylw.service;

import com.bylw.dto.LoginDTO;
import com.bylw.dto.RegisterDTO;
import com.bylw.dto.UpdateUserInfoDTO;
import com.bylw.entity.User;

public interface AuthService {
    String login(LoginDTO dto);
    User register(RegisterDTO dto);
    User getUserInfo(Integer userId);
    User updateUserInfo(Integer userId, UpdateUserInfoDTO dto);
    boolean resetPassword(String username, String phone, String newPassword);
}
