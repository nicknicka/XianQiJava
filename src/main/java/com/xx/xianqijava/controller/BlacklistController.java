package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Blacklist;
import com.xx.xianqijava.service.BlacklistService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 黑名单控制器
 */
@Slf4j
@Tag(name = "黑名单管理")
@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    /**
     * 添加黑名单
     */
    @Operation(summary = "添加黑名单")
    @PostMapping("/{blockedUserId}")
    public Result<Void> addToBlacklist(
            @Parameter(description = "被拉黑的用户ID") @PathVariable("blockedUserId") Long blockedUserId,
            @Parameter(description = "拉黑原因") @RequestParam(required = false) String reason) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("添加黑名单, userId={}, blockedUserId={}", userId, blockedUserId);
        blacklistService.addToBlacklist(userId, blockedUserId, reason);
        return Result.success("已添加到黑名单");
    }

    /**
     * 移除黑名单
     */
    @Operation(summary = "移除黑名单")
    @DeleteMapping("/{blacklistId}")
    public Result<Void> removeFromBlacklist(
            @Parameter(description = "黑名单ID") @PathVariable("blacklistId") Long blacklistId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("移除黑名单, userId={}, blacklistId={}", userId, blacklistId);
        blacklistService.removeFromBlacklist(userId, blacklistId);
        return Result.success("已移出黑名单");
    }

    /**
     * 获取黑名单列表
     */
    @Operation(summary = "获取黑名单列表")
    @GetMapping
    public Result<IPage<UserInfoVO>> getBlacklist(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询黑名单列表, userId={}, page={}, size={}", userId, page, size);

        Page<Blacklist> pageParam = new Page<>(page, size);
        IPage<UserInfoVO> blacklistPage = blacklistService.getBlacklist(userId, pageParam);

        return Result.success(blacklistPage);
    }

    /**
     * 检查用户是否在黑名单中
     */
    @Operation(summary = "检查用户是否在黑名单中")
    @GetMapping("/check/{targetUserId}")
    public Result<Boolean> isInBlacklist(
            @Parameter(description = "目标用户ID") @PathVariable("targetUserId") Long targetUserId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        boolean inBlacklist = blacklistService.isInBlacklist(userId, targetUserId);
        return Result.success(inBlacklist);
    }
}
