package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.OperationLog;
import com.xx.xianqijava.service.OperationLogService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@Slf4j
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/admin/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 获取操作日志列表（管理员）
     */
    @GetMapping("/list")
    @Operation(summary = "获取操作日志列表（管理员）")
    public Result<IPage<OperationLogVO>> getLogList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "模块") @RequestParam(required = false) String module,
            @Parameter(description = "操作类型") @RequestParam(required = false) String action,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        log.info("查询操作日志列表, userId={}, module={}, action={}, status={}",
                userId, module, action, status);

        Page<OperationLog> page = new Page<>(current, size);
        IPage<OperationLogVO> result = operationLogService.getLogList(
                page, userId, module, action, status, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 获取我的操作日志
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的操作日志")
    public Result<IPage<OperationLogVO>> getMyLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的操作日志, userId={}, page={}", userId, current);

        Page<OperationLog> page = new Page<>(current, size);
        IPage<OperationLogVO> result = operationLogService.getMyLogs(page, userId);
        return Result.success(result);
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/{logId}")
    @Operation(summary = "删除操作日志")
    public Result<Void> deleteLog(
            @Parameter(description = "日志ID") @PathVariable("logId") Long logId) {
        log.info("删除操作日志, logId={}", logId);
        operationLogService.deleteLog(logId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除操作日志
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除操作日志")
    public Result<Integer> batchDeleteLogs(
            @RequestBody java.util.List<Long> logIds) {
        log.info("批量删除操作日志, count={}", logIds.size());
        int count = operationLogService.batchDeleteLogs(logIds);
        return Result.success("成功删除 " + count + " 条日志", count);
    }

    /**
     * 清空所有日志
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有日志")
    public Result<Void> clearLogs() {
        log.info("清空所有操作日志");
        operationLogService.clearLogs();
        return Result.success("清空成功");
    }

    /**
     * 清理过期日志（管理员）
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清理过期日志（管理员）")
    public Result<Integer> cleanExpiredLogs(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "90") Integer days) {
        log.info("清理{}天前的操作日志", days);
        int count = operationLogService.cleanExpiredLogs(days);
        return Result.success(count);
    }
}
