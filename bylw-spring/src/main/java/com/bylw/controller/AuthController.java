package com.bylw.controller;

import com.bylw.common.Result;
import com.bylw.common.TokenBlocklist;
import com.bylw.dto.LoginDTO;
import com.bylw.dto.RegisterDTO;
import com.bylw.dto.UpdateUserInfoDTO;
import com.bylw.dto.UserResponse;
import com.bylw.entity.User;
import com.bylw.service.AuthService;
import com.bylw.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlocklist tokenBlocklist;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        String token = authService.login(dto);
        return Result.success(java.util.Map.of(
            "token", token,
            "role", jwtUtil.getRole(token),
            "roleType", jwtUtil.getRoleType(token)
        ));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        User user = authService.register(dto);
        return Result.success(UserResponse.from(user));
    }

    @GetMapping("/userinfo")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Integer userId = getUserIdFromToken(request);
        return Result.success(UserResponse.from(authService.getUserInfo(userId)));
    }

    @PutMapping("/userinfo")
    public Result<?> updateUserInfo(HttpServletRequest request, @Valid @RequestBody UpdateUserInfoDTO dto) {
        Integer userId = getUserIdFromToken(request);
        return Result.success(UserResponse.from(authService.updateUserInfo(userId, dto)));
    }

    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String phone = body.get("phone");
        String newPassword = body.get("newPassword");
        return Result.success(authService.resetPassword(username, phone, newPassword));
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                long expiresAt = claims.getExpiration().getTime();
                tokenBlocklist.revoke(token, expiresAt);
            } catch (Exception ignored) {
                // token 已过期或无效，无需加入黑名单
            }
        }
        return Result.success(null);
    }

    private Integer getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未登录，请先获取令牌");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
