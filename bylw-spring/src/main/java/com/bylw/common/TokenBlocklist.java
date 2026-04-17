package com.bylw.common;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT Token 黑名单
 * 用于 logout 时将 token 加入黑名单，拦截器自动拦截已吊销的 token。
 * 定时清理已过期的条目，避免内存泄漏。
 */
@Component
public class TokenBlocklist {

    // token -> 过期时间戳(ms)
    private final Map<String, Long> blocklist = new ConcurrentHashMap<>();

    /**
     * 将 token 加入黑名单
     */
    public void revoke(String token, long expiresAtMillis) {
        blocklist.put(token, expiresAtMillis);
    }

    /**
     * 检查 token 是否已被吊销
     */
    public boolean isRevoked(String token) {
        Long expiresAt = blocklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiresAt) {
            blocklist.remove(token);
            return false;
        }
        return true;
    }

    /**
     * 每小时清理已过期条目
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        blocklist.entrySet().removeIf(entry -> now > entry.getValue());
    }
}
