package com.bylw.common;

import com.bylw.entity.User;
import com.bylw.mapper.UserMapper;
import com.bylw.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 认证工具类 - 统一处理 token 解析和权限验证
 *
 * 移除了所有 Controller 中的重复认证逻辑。
 */
@Component
public class AuthUtil {

    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 从请求中获取当前用户ID
     *
     * @param request HTTP请求
     * @return 用户ID
     * @throws IllegalArgumentException 未登录时抛出
     */
    public Integer getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        Integer userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new IllegalArgumentException("无效的用户令牌");
        }
        return userId;
    }

    /**
     * 从请求中获取当前用户角色
     *
     * @param request HTTP请求
     * @return 用户角色，未登录时返回null
     */
    public String getRole(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            return jwtUtil.getRole(token);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 从请求中获取当前用户的角色类型
     *
     * @param request HTTP请求
     * @return 角色类型，未登录时返回null
     */
    public Integer getRoleType(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            return jwtUtil.getRoleType(token);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 验证当前用户是否为管理员
     *
     * @param request HTTP请求
     * @throws IllegalArgumentException 未登录或非管理员时抛出
     */
    public void verifyAdmin(HttpServletRequest request) {
        String role = getRole(request);
        if (role == null) {
            throw new IllegalArgumentException("未登录，请先获取令牌");
        }
        if (!"admin".equals(role)) {
            throw new IllegalArgumentException("权限不足，仅管理员可访问");
        }
    }

    /**
     * 检查当前用户是否为管理员（包括特殊角色类型99）
     *
     * @param request HTTP请求
     * @return 是否为管理员
     */
    public boolean isAdmin(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            String role = jwtUtil.getRole(token);
            Integer roleType = jwtUtil.getRoleType(token);
            return "admin".equals(role) || (roleType != null && roleType == 99);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查当前用户是否为骑手
     *
     * @param request HTTP请求
     * @return 是否为骑手
     */
    public boolean isCourier(HttpServletRequest request) {
        try {
            Integer roleType = getRoleType(request);
            boolean result = roleType != null && roleType == 2;
            log.info("[isCourier] roleType={}, result={}", roleType, result);
            return result;
        } catch (Exception e) {
            log.error("[isCourier] error", e);
            return false;
        }
    }

    /**
     * 获取当前用户的手机号
     *
     * @param request HTTP请求
     * @return 手机号，未找到返回null
     */
    public String getPhone(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            User user = userMapper.selectById(userId);
            String phone = user != null ? user.getPhone() : null;
            log.info("[getPhone] userId={}", userId);
            return phone;
        } catch (Exception e) {
            log.error("[getPhone] error", e);
            return null;
        }
    }

    /**
     * 获取当前用户的昵称
     *
     * @param request HTTP请求
     * @return 昵称，未找到返回null
     */
    public String getNickname(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            User user = userMapper.selectById(userId);
            String nickname = user != null ? user.getNickname() : null;
            log.info("[getNickname] userId={}, nickname={}", userId, nickname);
            return nickname;
        } catch (Exception e) {
            log.error("[getNickname] error", e);
            return null;
        }
    }

    /**
     * 验证当前用户是否已登录
     *
     * @param request HTTP请求
     * @throws IllegalArgumentException 未登录时抛出
     */
    public void verifyLogin(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("未登录，请先获取令牌");
        }
    }

    /**
     * 从请求头中提取 Bearer Token
     *
     * @param request HTTP请求
     * @return token字符串
     * @throws IllegalArgumentException token不存在或格式错误时抛出
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("未登录，请先获取令牌");
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }
}
