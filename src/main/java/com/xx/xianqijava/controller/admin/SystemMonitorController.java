package com.xx.xianqijava.controller.admin;

import com.xx.xianqijava.service.SystemMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统监控控制器 - 管理端
 */
@Slf4j
@Tag(name = "系统监控", description = "系统监控相关接口")
@RestController
@RequestMapping("/admin/monitor")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-auth")
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;

    /**
     * 获取系统健康状态
     */
    @Operation(summary = "获取系统健康状态", description = "检查数据库、Redis、磁盘等组件健康状态")
    @GetMapping("/health")
    public Map<String, Object> getHealthStatus() {
        log.info("获取系统健康状态");
        return systemMonitorService.getHealthStatus();
    }

    /**
     * 获取系统性能指标
     */
    @Operation(summary = "获取系统性能指标", description = "获取CPU、内存、线程等性能指标")
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceMetrics() {
        log.info("获取系统性能指标");
        return systemMonitorService.getPerformanceMetrics();
    }

    /**
     * 获取内存信息
     */
    @Operation(summary = "获取内存信息", description = "获取JVM堆内存和非堆内存详细信息")
    @GetMapping("/memory")
    public Map<String, Object> getMemoryInfo() {
        log.info("获取内存信息");
        return systemMonitorService.getMemoryInfo();
    }

    /**
     * 获取线程信息
     */
    @Operation(summary = "获取线程信息", description = "获取JVM线程相关信息")
    @GetMapping("/threads")
    public Map<String, Object> getThreadInfo() {
        log.info("获取线程信息");
        return systemMonitorService.getThreadInfo();
    }

    /**
     * 获取应用信息
     */
    @Operation(summary = "获取应用信息", description = "获取应用基本信息和运行环境")
    @GetMapping("/application")
    public Map<String, Object> getApplicationInfo() {
        log.info("获取应用信息");
        return systemMonitorService.getApplicationInfo();
    }

    /**
     * 获取性能统计
     */
    @Operation(summary = "获取性能统计", description = "获取最近一段时间的接口性能统计数据")
    @GetMapping("/performance/statistics")
    public Map<String, Object> getPerformanceStatistics(
            @RequestParam(defaultValue = "60") Integer minutes) {
        log.info("获取性能统计, minutes={}", minutes);
        return systemMonitorService.getPerformanceStatistics(minutes);
    }
}
