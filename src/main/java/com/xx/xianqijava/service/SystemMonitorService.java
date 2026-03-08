package com.xx.xianqijava.service;

import java.lang.management.MemoryUsage;
import java.util.Map;

/**
 * 系统监控服务接口
 */
public interface SystemMonitorService {

    /**
     * 获取系统健康状态
     *
     * @return 健康状态信息
     */
    Map<String, Object> getHealthStatus();

    /**
     * 获取系统性能指标
     *
     * @return 性能指标信息
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * 获取 JVM 内存信息
     *
     * @return 内存信息
     */
    Map<String, Object> getMemoryInfo();

    /**
     * 获取线程信息
     *
     * @return 线程信息
     */
    Map<String, Object> getThreadInfo();

    /**
     * 获取应用信息
     *
     * @return 应用信息
     */
    Map<String, Object> getApplicationInfo();

    /**
     * 记录性能指标
     *
     * @param endpoint   接口路径
     * @param executeTime 执行时间
     * @param status     执行状态
     */
    void recordPerformance(String endpoint, long executeTime, int status);

    /**
     * 获取最近性能统计
     *
     * @param minutes 统计最近多少分钟
     * @return 性能统计信息
     */
    Map<String, Object> getPerformanceStatistics(int minutes);
}
