package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.common.Constants;
import com.bylw.dto.LoginDTO;
import com.bylw.dto.RegisterDTO;
import com.bylw.dto.UpdateUserInfoDTO;
import com.bylw.entity.Courier;
import com.bylw.entity.User;
import com.bylw.mapper.CourierMapper;
import com.bylw.mapper.UserMapper;
import com.bylw.service.AuthService;
import com.bylw.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourierMapper courierMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername())
               .eq(User::getDeleted, 0);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        String storedPwd = user.getPassword();
        // BCrypt 优先：新用户（register 时使用 BCrypt 存 password）
        if (storedPwd.startsWith("$2")) {
            if (!encoder.matches(dto.getPassword(), storedPwd)) {
                throw new IllegalArgumentException("密码错误");
            }
            // 已是 BCrypt，无需迁移
        } else {
            // 遗留 MD5 用户：登录时自动迁移到 BCrypt
            String md5Pwd = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
            if (!md5Pwd.equals(storedPwd)) {
                throw new IllegalArgumentException("密码错误");
            }
            user.setPassword(encoder.encode(dto.getPassword()));
            userMapper.updateById(user);
        }

        return jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole(), user.getRoleType());
    }

    @Override
    public User register(RegisterDTO dto) {
        validatePasswordStrength(dto.getPassword());
        if (dto.getUsername() == null || dto.getUsername().length() < 3) {
            throw new IllegalArgumentException("用户名长度不能少于3位");
        }
        if (dto.getPhone() != null && !dto.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (dto.getEmail() != null && !dto.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername())
               .eq(User::getDeleted, 0);
        if (userMapper.selectOne(wrapper) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(Constants.ROLE_USER);
        user.setRoleType(dto.getRoleType() != null ? dto.getRoleType() : 1);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);

        userMapper.insert(user);

        if (user.getRoleType() != null && user.getRoleType() == 2) {
            Courier courier = new Courier();
            courier.setUserId(user.getUserId());
            courier.setCourierName(user.getNickname());
            courier.setCourierPhone(user.getPhone());
            courier.setStatus(1);
            courier.setCreateTime(LocalDateTime.now());
            courierMapper.insert(courier);
        }

        return user;
    }

    @Override
    public User getUserInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    @Override
    public User updateUserInfo(Integer userId, UpdateUserInfoDTO dto) {
        User existing = getUserInfo(userId);

        boolean isCourier = existing.getRoleType() != null && existing.getRoleType() == 2;
        if (dto.getNickname() != null) {
            existing.setNickname(normalize(dto.getNickname()));
        }
        if (dto.getGender() != null) {
            existing.setGender(normalize(dto.getGender()));
        }
        if (dto.getPhone() != null) {
            existing.setPhone(normalize(dto.getPhone()));
        }
        if (dto.getEmail() != null) {
            existing.setEmail(normalize(dto.getEmail()));
        }
        if (dto.getAvatar() != null) {
            existing.setAvatar(normalize(dto.getAvatar()));
        }
        if (dto.getAddress() != null) {
            existing.setAddress(normalize(dto.getAddress()));
        }
        existing.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(existing);

        if (isCourier) {
            LambdaQueryWrapper<Courier> cWrapper = new LambdaQueryWrapper<>();
            cWrapper.eq(Courier::getUserId, userId);
            Courier courier = courierMapper.selectOne(cWrapper);
            if (courier != null) {
                courier.setCourierName(existing.getNickname());
                courier.setCourierPhone(existing.getPhone());
                courierMapper.updateById(courier);
            }
        }

        return userMapper.selectById(userId);
    }

    @Override
    public boolean resetPassword(String username, String phone, String newPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("新密码长度不能少于8位");
        }
        validatePasswordStrength(newPassword);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getPhone, phone)
               .eq(User::getDeleted, 0);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalArgumentException("用户名与手机号不匹配");
        }
        user.setPassword(encoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return true;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 密码强度验证：至少8位，包含大小写字母、数字和特殊字符
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("密码长度不能少于8位");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("密码必须包含至少一个大写字母");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("密码必须包含至少一个小写字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("密码必须包含至少一个数字");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("密码必须包含至少一个特殊字符（如 !@#$%^&*）");
        }
    }
}
