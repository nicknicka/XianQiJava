package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
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
@RequestMapping("/admin/statistics")
@Tag(name = "数据统计管理", description = "数据统计相关接口")
@SecurityRequirement(name = "bearer-auth")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取总览统计数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取总览统计数据", description = "获取平台总览统计数据，用于管理后台首页展示")
    public Result<StatisticsVO> getOverviewStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取总览统计数据", userId);
        StatisticsVO data = statisticsService.getOverviewStatistics();
        return Result.success(data);
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户统计数据", description = "获取用户相关的详细统计数据")
    public Result<UserStatisticsVO> getUserStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取用户统计数据", userId);
        UserStatisticsVO data = statisticsService.getUserStatistics();
        return Result.success(data);
    }

    /**
     * 获取商品统计数据
     */
    @GetMapping("/products")
    @Operation(summary = "获取商品统计数据", description = "获取商品相关的详细统计数据")
    public Result<ProductStatisticsVO> getProductStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取商品统计数据", userId);
        ProductStatisticsVO data = statisticsService.getProductStatistics();
        return Result.success(data);
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/orders")
    @Operation(summary = "获取订单统计数据", description = "获取订单相关的详细统计数据")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("用户{}获取订单统计数据", userId);
        OrderStatisticsVO data = statisticsService.getOrderStatistics();
        return Result.success(data);
    }
}
