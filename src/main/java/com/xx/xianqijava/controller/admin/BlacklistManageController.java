package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Blacklist;
import com.xx.xianqijava.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 黑名单管理控制器（管理端）
 */
@Slf4j
@Tag(name = "黑名单管理（管理端）")
@RestController
@RequestMapping("/admin/blacklist")
@RequiredArgsConstructor
public class BlacklistManageController {

    private final BlacklistService blacklistService;

    /**
     * 分页查询黑名单列表（管理员）
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询黑名单列表")
    public Result<IPage<Blacklist>> getBlacklistList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "被拉黑用户ID") @RequestParam(required = false) Long blockedUserId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        log.info("查询黑名单列表（管理员）, page={}, size={}, userId={}, blockedUserId={}",
                page, size, userId, blockedUserId);

        Page<Blacklist> pageParam = new Page<>(page, size);
        IPage<Blacklist> result = blacklistService.getBlacklistList(
                pageParam, userId, blockedUserId, keyword, startTime, endTime);

        return Result.success(result);
    }

    /**
     * 获取黑名单详情
     */
    @GetMapping("/{blacklistId}")
    @Operation(summary = "获取黑名单详情")
    public Result<Blacklist> getBlacklistDetail(
            @Parameter(description = "黑名单ID") @PathVariable("blacklistId") Long blacklistId) {
        log.info("查询黑名单详情, blacklistId={}", blacklistId);
        Blacklist blacklist = blacklistService.getById(blacklistId);
        if (blacklist == null) {
            return Result.error("黑名单记录不存在");
        }
        return Result.success(blacklist);
    }

    /**
     * 删除黑名单
     */
    @DeleteMapping("/{blacklistId}")
    @Operation(summary = "删除黑名单")
    public Result<Boolean> deleteBlacklist(
            @Parameter(description = "黑名单ID") @PathVariable("blacklistId") Long blacklistId) {
        log.info("删除黑名单, blacklistId={}", blacklistId);
        boolean success = blacklistService.removeById(blacklistId);
        return Result.success(success);
    }

    /**
     * 批量删除黑名单
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除黑名单")
    public Result<Integer> batchDeleteBlacklists(@RequestBody java.util.List<Long> blacklistIds) {
        log.info("批量删除黑名单, count={}", blacklistIds.size());
        int count = 0;
        for (Long blacklistId : blacklistIds) {
            if (blacklistService.removeById(blacklistId)) {
                count++;
            }
        }
        return Result.success(count);
    }

    /**
     * 获取黑名单统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取黑名单统计信息")
    public Result<Map<String, Object>> getStatistics() {
        log.info("查询黑名单统计信息");

        Map<String, Object> statistics = new HashMap<>();

        // 总数统计
        long totalCount = blacklistService.count();
        statistics.put("totalCount", totalCount);

        // 今日新增
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        long todayCount = blacklistService.lambdaQuery()
                .ge(Blacklist::getCreateTime, todayStart)
                .lt(Blacklist::getCreateTime, todayEnd)
                .count();
        statistics.put("todayCount", todayCount);

        // 本周新增
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        long weekCount = blacklistService.lambdaQuery()
                .ge(Blacklist::getCreateTime, weekStart)
                .count();
        statistics.put("weekCount", weekCount);

        // 本月新增
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        long monthCount = blacklistService.lambdaQuery()
                .ge(Blacklist::getCreateTime, monthStart)
                .count();
        statistics.put("monthCount", monthCount);

        return Result.success(statistics);
    }
}
