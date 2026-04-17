package com.bylw.config;

import com.bylw.common.TokenBlocklist;
import com.bylw.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * JWT Token 黑名单拦截器
 * 拦截所有 /api/** 请求，检查 token 是否已被吊销。
 * 放行无需认证的路径（登录、注册、公开资源等）。
 */
@Component
public class TokenBlocklistInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private TokenBlocklist tokenBlocklist;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return true; // 无 token 的请求由各 Controller 自行处理
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        if (tokenBlocklist.isRevoked(token)) {
            writeUnauthorized(response, "登录已失效，请重新登录");
            return false;
        }

        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(
            Map.of("code", 401, "message", message, "data", null)
        ));
    }
}
