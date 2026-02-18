package com.xx.xianqijava.controller;

import com.xx.xianqijava.service.StatisticsService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.OrderStatisticsVO;
import com.xx.xianqijava.vo.ProductStatisticsVO;
import com.xx.xianqijava.vo.StatisticsVO;
import com.xx.xianqijava.vo.UserStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/statistics")
@Tag(name = "数据统计管理", description = "数据统计相关接口")
@SecurityRequirement(name = "bearer-auth")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取总览统计数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取总览统计数据", description = "获取平台总览统计数据，用于管理后台首页展示")
    public StatisticsVO getOverviewStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取总览统计数据", userId);
        return statisticsService.getOverviewStatistics();
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户统计数据", description = "获取用户相关的详细统计数据")
    public UserStatisticsVO getUserStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取用户统计数据", userId);
        return statisticsService.getUserStatistics();
    }

    /**
     * 获取商品统计数据
     */
    @GetMapping("/products")
    @Operation(summary = "获取商品统计数据", description = "获取商品相关的详细统计数据")
    public ProductStatisticsVO getProductStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取商品统计数据", userId);
        return statisticsService.getProductStatistics();
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/orders")
    @Operation(summary = "获取订单统计数据", description = "获取订单相关的详细统计数据")
    public OrderStatisticsVO getOrderStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取订单统计数据", userId);
        return statisticsService.getOrderStatistics();
    }
}
