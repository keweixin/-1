package com.bylw.service;

/**
 * 管理员统计服务 - 提供后台仪表盘所需的各种统计数据
 */
public interface AdminStatsService {
    /**
     * 获取管理员统计数据
     */
    com.bylw.dto.AdminStatsDTO getAdminStats();
}