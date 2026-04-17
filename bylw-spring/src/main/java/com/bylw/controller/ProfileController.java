package com.bylw.controller;

import com.bylw.common.Result;
import com.bylw.dto.FoodProfileDTO;
import com.bylw.entity.FoodProfile;
import com.bylw.service.FoodProfileService;
import com.bylw.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private FoodProfileService foodProfileService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<?> getProfile(HttpServletRequest request) {
        Integer userId = getUserId(request);
        return Result.success(foodProfileService.getProfile(userId));
    }

    @PostMapping
    public Result<?> saveProfile(HttpServletRequest request, @RequestBody FoodProfileDTO dto) {
        dto.setUserId(getUserId(request));
        return Result.success(foodProfileService.saveOrUpdate(dto));
    }

    private Integer getUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未登录，请先获取令牌");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
