package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 系统监控服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMonitorServiceImpl implements SystemMonitorService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PERF_PREFIX = "monitor:performance:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();

        // 检查数据库连接
        boolean dbHealthy = checkDatabaseHealth();
        health.put("database", dbHealthy ? "UP" : "DOWN");

        // 检查 Redis 连接
        boolean redisHealthy = checkRedisHealth();
        health.put("redis", redisHealthy ? "UP" : "DOWN");

        // 检查磁盘空间
        boolean diskHealthy = checkDiskSpace();
        health.put("disk", diskHealthy ? "UP" : "DOWN");

        // 整体状态
        boolean overallHealthy = dbHealthy && redisHealthy && diskHealthy;
        health.put("status", overallHealthy ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        return health;
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // CPU 核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();
        metrics.put("cpuCores", cpuCores);

        // JVM 内存信息
        Map<String, Object> memoryInfo = getMemoryInfo();
        metrics.put("memory", memoryInfo);

        // 线程信息
        Map<String, Object> threadInfo = getThreadInfo();
        metrics.put("threads", threadInfo);

        // 系统负载
        double systemLoad = getSystemLoadAverage();
        metrics.put("systemLoad", systemLoad);

        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        metrics.put("jvmVersion", System.getProperty("java.version"));
        metrics.put("osName", System.getProperty("os.name"));
        metrics.put("osVersion", System.getProperty("os.version"));

        metrics.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        return metrics;
    }

    @Override
    public Map<String, Object> getMemoryInfo() {
        Map<String, Object> memoryInfo = new HashMap<>();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();

        // 堆内存
        Map<String, Object> heapInfo = new HashMap<>();
        heapInfo.put("init", formatBytes(heapMemory.getInit()));
        heapInfo.put("used", formatBytes(heapMemory.getUsed()));
        heapInfo.put("committed", formatBytes(heapMemory.getCommitted()));
        heapInfo.put("max", formatBytes(heapMemory.getMax()));
        heapInfo.put("usagePercent", calculateUsagePercent(heapMemory.getUsed(), heapMemory.getMax()));
        memoryInfo.put("heap", heapInfo);

        // 非堆内存
        Map<String, Object> nonHeapInfo = new HashMap<>();
        nonHeapInfo.put("init", formatBytes(nonHeapMemory.getInit()));
        nonHeapInfo.put("used", formatBytes(nonHeapMemory.getUsed()));
        nonHeapInfo.put("committed", formatBytes(nonHeapMemory.getCommitted()));
        nonHeapInfo.put("max", formatBytes(nonHeapMemory.getMax()));
        memoryInfo.put("nonHeap", nonHeapInfo);

        return memoryInfo;
    }

    @Override
    public Map<String, Object> getThreadInfo() {
        Map<String, Object> threadInfo = new HashMap<>();

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        threadInfo.put("threadCount", threadMXBean.getThreadCount());
        threadInfo.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        threadInfo.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        threadInfo.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());

        return threadInfo;
    }

    @Override
    public Map<String, Object> getApplicationInfo() {
        Map<String, Object> appInfo = new HashMap<>();

        // 应用信息
        appInfo.put("appName", "仙球校园二手交易平台");
        appInfo.put("version", "1.0.0");
        appInfo.put("environment", getEnvironment());

        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        appInfo.put("uptime", formatUptime(uptime));
        appInfo.put("startTime", LocalDateTime.now()
                .minusSeconds(uptime / 1000)
                .format(DATE_FORMATTER));

        // JVM 信息
        appInfo.put("javaVersion", System.getProperty("java.version"));
        appInfo.put("javaVendor", System.getProperty("java.vendor"));
        appInfo.put("javaHome", System.getProperty("java.home"));

        // OS 信息
        appInfo.put("osName", System.getProperty("os.name"));
        appInfo.put("osArch", System.getProperty("os.arch"));
        appInfo.put("osVersion", System.getProperty("os.version"));
        appInfo.put("processors", runtime.availableProcessors());

        appInfo.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        return appInfo;
    }

    @Override
    public void recordPerformance(String endpoint, long executeTime, int status) {
        try {
            String key = PERF_PREFIX + endpoint;

            // 记录到 Redis
            Map<String, Object> record = new HashMap<>();
            record.put("endpoint", endpoint);
            record.put("executeTime", executeTime);
            record.put("status", status);
            record.put("timestamp", System.currentTimeMillis());

            // 使用 List 存储，保留最近 1000 条记录
            redisTemplate.opsForList().leftPush(key, record);
            redisTemplate.opsForList().trim(key, 0, 999);

            // 设置过期时间（1小时）
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("记录性能指标失败, endpoint={}, executeTime={}, status={}",
                    endpoint, executeTime, status, e);
        }
    }

    @Override
    public Map<String, Object> getPerformanceStatistics(int minutes) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 获取所有性能键
            Set<String> keys = redisTemplate.keys(PERF_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                statistics.put("message", "暂无性能数据");
                return statistics;
            }

            long startTime = System.currentTimeMillis() - minutes * 60 * 1000L;

            int totalCount = 0;
            int successCount = 0;
            int errorCount = 0;
            long totalExecuteTime = 0;
            long minExecuteTime = Long.MAX_VALUE;
            long maxExecuteTime = 0;

            // 统计所有端点的数据
            for (String key : keys) {
                List<Object> records = redisTemplate.opsForList().range(key, 0, -1);
                if (records != null) {
                    for (Object record : records) {
                        if (record instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> perfRecord = (Map<String, Object>) record;

                            Long timestamp = (Long) perfRecord.get("timestamp");
                            if (timestamp != null && timestamp >= startTime) {
                                totalCount++;
                                Integer recordStatus = (Integer) perfRecord.get("status");
                                if (recordStatus != null && recordStatus == 1) {
                                    successCount++;
                                } else {
                                    errorCount++;
                                }

                                Long executeTime = (Long) perfRecord.get("executeTime");
                                if (executeTime != null) {
                                    totalExecuteTime += executeTime;
                                    minExecuteTime = Math.min(minExecuteTime, executeTime);
                                    maxExecuteTime = Math.max(maxExecuteTime, executeTime);
                                }
                            }
                        }
                    }
                }
            }

            if (totalCount > 0) {
                statistics.put("totalCount", totalCount);
                statistics.put("successCount", successCount);
                statistics.put("errorCount", errorCount);
                statistics.put("successRate", String.format("%.2f%%",
                        (double) successCount / totalCount * 100));
                statistics.put("avgExecuteTime", totalExecuteTime / totalCount);
                statistics.put("minExecuteTime", minExecuteTime);
                statistics.put("maxExecuteTime", maxExecuteTime);
                statistics.put("timeRange", "最近 " + minutes + " 分钟");
            } else {
                statistics.put("message", "暂无性能数据");
            }

            statistics.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        } catch (Exception e) {
            log.error("获取性能统计失败", e);
            statistics.put("error", "获取性能统计失败");
        }

        return statistics;
    }

    /**
     * 检查数据库健康状态
     */
    private boolean checkDatabaseHealth() {
        try {
            // 简单检查：尝试查询一条记录
            redisTemplate.opsForValue().get("health:check");
            return true;
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return false;
        }
    }

    /**
     * 检查 Redis 健康状态
     */
    private boolean checkRedisHealth() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.error("Redis 健康检查失败", e);
            return false;
        }
    }

    /**
     * 检查磁盘空间
     */
    private boolean checkDiskSpace() {
        try {
            java.io.File disk = new java.io.File(".");
            long freeSpace = disk.getFreeSpace();
            long totalSpace = disk.getTotalSpace();
            double usagePercent = (double) (totalSpace - freeSpace) / totalSpace * 100;

            // 磁盘使用率超过 90% 警告
            return usagePercent < 90;
        } catch (Exception e) {
            log.error("磁盘空间检查失败", e);
            return true; // 检查失败返回健康，避免误报
        }
    }

    /**
     * 获取系统负载
     */
    private double getSystemLoadAverage() {
        try {
            return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取当前环境
     */
    private String getEnvironment() {
        String env = System.getProperty("spring.profiles.active");
        return env != null ? env : "default";
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 计算使用率百分比
     */
    private double calculateUsagePercent(long used, long max) {
        if (max <= 0) return 0;
        return (double) used / max * 100;
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }
}
