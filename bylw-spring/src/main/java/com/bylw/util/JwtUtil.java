package com.bylw.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private static final String DEV_SECRET = "bylw-dev-only-replace-in-production-32chars";

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT 密钥未配置：请设置环境变量 JWT_SECRET（建议 >= 32 字符）");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT 密钥强度不足：长度应 >= 32 字符");
        }
        if (DEV_SECRET.equals(secret)) {
            System.err.println("[WARN] 使用了开发默认 JWT 密钥，请生产环境设置 JWT_SECRET 环境变量");
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Integer userId, String username, String role, Integer roleType) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("username", username, "role", role, "roleType", roleType))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer getUserId(String token) {
        String subject = parseToken(token).getSubject();
        if (subject == null || subject.isBlank()) {
            return null;
        }
        return Integer.parseInt(subject);
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Integer getRoleType(String token) {
        return parseToken(token).get("roleType", Integer.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
