package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.SystemNotification;
import com.xx.xianqijava.service.SystemNotificationService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.SystemNotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统通知控制器
 */
@Slf4j
@Tag(name = "系统通知管理")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class SystemNotificationController {

    private final SystemNotificationService systemNotificationService;

    /**
     * 获取通知列表
     */
    @GetMapping
    @Operation(summary = "获取通知列表")
    public Result<IPage<SystemNotificationVO>> getNotificationList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询通知列表, userId={}, page={}, size={}", userId, page, size);

        Page<SystemNotification> pageParam = new Page<>(page, size);
        IPage<SystemNotificationVO> notificationPage = systemNotificationService.getNotificationList(userId, pageParam);

        return Result.success(notificationPage);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "获取通知详情")
    public Result<SystemNotificationVO> getNotificationDetail(
            @Parameter(description = "通知ID") @PathVariable("notificationId") Long notificationId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询通知详情, notificationId={}, userId={}", notificationId, userId);

        SystemNotificationVO notificationVO = systemNotificationService.getNotificationDetail(notificationId, userId);

        // 自动标记为已读
        systemNotificationService.markAsRead(notificationId, userId);

        return Result.success(notificationVO);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Integer> getUnreadCount() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询未读通知数量, userId={}", userId);

        Integer unreadCount = systemNotificationService.getUnreadCount(userId);
        return Result.success(unreadCount);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "标记所有通知为已读")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("标记所有通知为已读, userId={}", userId);

        systemNotificationService.markAllAsRead(userId);
        return Result.success("已标记所有通知为已读");
    }
}
