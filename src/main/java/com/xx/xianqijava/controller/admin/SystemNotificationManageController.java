package com.xx.xianqijava.controller.admin;

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

import java.time.LocalDateTime;

/**
 * 管理员端 - 系统通知管理控制器
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@RestController
@RequestMapping("/admin/system-notification")
@RequiredArgsConstructor
@Tag(name = "管理员-系统通知管理", description = "管理员管理系统通知的接口")
public class SystemNotificationManageController {

    private final SystemNotificationService systemNotificationService;

    /**
     * 创建系统通知
     */
    @PostMapping
    @Operation(summary = "创建系统通知", description = "管理员创建新的系统通知")
    public Result<SystemNotification> createNotification(@RequestBody SystemNotification notification) {
        log.info("创建系统通知, title={}", notification.getTitle());

        // 设置创建时间
        notification.setCreateTime(LocalDateTime.now());

        // 如果状态为已发布，设置发布时间
        if (notification.getStatus() != null && notification.getStatus() == 1) {
            notification.setPublishTime(LocalDateTime.now());
        }

        boolean saved = systemNotificationService.save(notification);
        if (!saved) {
            return Result.error("创建通知失败");
        }

        log.info("系统通知创建成功, notificationId={}", notification.getNotificationId());
        return Result.success(notification, "创建通知成功");
    }

    /**
     * 更新系统通知
     */
    @PutMapping("/{notificationId}")
    @Operation(summary = "更新系统通知", description = "管理员更新系统通知内容")
    public Result<SystemNotification> updateNotification(
            @PathVariable Long notificationId,
            @RequestBody SystemNotification notification) {
        log.info("更新系统通知, notificationId={}", notificationId);

        SystemNotification existing = systemNotificationService.getById(notificationId);
        if (existing == null) {
            return Result.error("通知不存在");
        }

        // 设置通知ID
        notification.setNotificationId(notificationId);

        // 如果状态从未发布变为已发布，设置发布时间
        if (existing.getStatus() == null || existing.getStatus() != 1) {
            if (notification.getStatus() != null && notification.getStatus() == 1) {
                notification.setPublishTime(LocalDateTime.now());
            }
        }

        boolean updated = systemNotificationService.updateById(notification);
        if (!updated) {
            return Result.error("更新通知失败");
        }

        log.info("系统通知更新成功, notificationId={}", notificationId);
        return Result.success(notification, "更新通知成功");
    }

    /**
     * 发布系统通知
     */
    @PutMapping("/{notificationId}/publish")
    @Operation(summary = "发布系统通知", description = "管理员发布系统通知")
    public Result<Void> publishNotification(@PathVariable Long notificationId) {
        log.info("发布系统通知, notificationId={}", notificationId);

        SystemNotification notification = systemNotificationService.getById(notificationId);
        if (notification == null) {
            return Result.error("通知不存在");
        }

        notification.setStatus(1); // 已发布
        notification.setPublishTime(LocalDateTime.now());

        boolean updated = systemNotificationService.updateById(notification);
        if (!updated) {
            return Result.error("发布通知失败");
        }

        log.info("系统通知发布成功, notificationId={}", notificationId);
        return Result.success("发布通知成功");
    }

    /**
     * 删除系统通知
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除系统通知", description = "管理员删除系统通知（逻辑删除）")
    public Result<Void> deleteNotification(@PathVariable Long notificationId) {
        log.info("删除系统通知, notificationId={}", notificationId);

        SystemNotification notification = systemNotificationService.getById(notificationId);
        if (notification == null) {
            return Result.error("通知不存在");
        }

        // 逻辑删除：设置状态为已删除
        notification.setStatus(0); // 已删除

        boolean deleted = systemNotificationService.updateById(notification);
        if (!deleted) {
            return Result.error("删除通知失败");
        }

        log.info("系统通知删除成功, notificationId={}", notificationId);
        return Result.success("删除通知成功");
    }

    /**
     * 获取通知列表（管理员）
     */
    @GetMapping
    @Operation(summary = "获取通知列表", description = "管理员查看所有系统通知")
    public Result<IPage<SystemNotification>> getNotificationList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "通知状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "通知类型") @RequestParam(required = false) Integer type) {

        log.info("管理员查询通知列表, page={}, size={}, status={}, type={}", page, size, status, type);

        Page<SystemNotification> pageParam = new Page<>(page, size);
        IPage<SystemNotification> notificationPage = systemNotificationService.page(pageParam);

        return Result.success(notificationPage);
    }

    /**
     * 获取通知详情（管理员）
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "获取通知详情", description = "管理员查看系统通知详情")
    public Result<SystemNotification> getNotificationDetail(@PathVariable Long notificationId) {
        log.info("管理员查询通知详情, notificationId={}", notificationId);

        SystemNotification notification = systemNotificationService.getById(notificationId);
        if (notification == null) {
            return Result.error("通知不存在");
        }

        return Result.success(notification);
    }
}
