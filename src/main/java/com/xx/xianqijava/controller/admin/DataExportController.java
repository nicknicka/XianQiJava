package com.xx.xianqijava.controller.admin;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.DataExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 数据导出控制器 - 管理端
 */
@Slf4j
@Tag(name = "数据导出", description = "数据导出相关接口")
@RestController
@RequestMapping("/admin/export")
@RequiredArgsConstructor
public class DataExportController {

    private final DataExportService dataExportService;

    /**
     * 导出订单数据
     */
    @Operation(summary = "导出订单数据", description = "导出订单数据为 CSV 文件")
    @GetMapping("/orders")
    public void exportOrders(
            HttpServletResponse response,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status) throws IOException {
        log.info("导出订单数据请求, startTime={}, endTime={}, status={}", startTime, endTime, status);
        dataExportService.exportOrdersToCsv(response, startTime, endTime, status);
    }

    /**
     * 导出用户数据
     */
    @Operation(summary = "导出用户数据", description = "导出用户数据为 CSV 文件")
    @GetMapping("/users")
    public void exportUsers(
            HttpServletResponse response,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status) throws IOException {
        log.info("导出用户数据请求, keyword={}, status={}", keyword, status);
        dataExportService.exportUsersToCsv(response, keyword, status);
    }

    /**
     * 导出统计数据
     */
    @Operation(summary = "导出统计数据", description = "导出统计数据为 CSV 文件")
    @GetMapping("/statistics")
    public void exportStatistics(
            HttpServletResponse response,
            @Parameter(description = "统计天数") @RequestParam(required = false, defaultValue = "7") Integer days) throws IOException {
        log.info("导出统计数据请求, days={}", days);
        dataExportService.exportStatisticsToCsv(response, days);
    }
}
