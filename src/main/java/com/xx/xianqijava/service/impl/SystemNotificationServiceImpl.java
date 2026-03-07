package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.NotificationReadRecord;
import com.xx.xianqijava.entity.SystemNotification;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.NotificationReadRecordMapper;
import com.xx.xianqijava.mapper.SystemNotificationMapper;
import com.xx.xianqijava.service.SystemNotificationService;
import com.xx.xianqijava.vo.SystemNotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统通知服务实现类
 */
@Slf4j
@Service
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification>
        implements SystemNotificationService {

    private final NotificationReadRecordMapper notificationReadRecordMapper;

    public SystemNotificationServiceImpl(NotificationReadRecordMapper notificationReadRecordMapper) {
        this.notificationReadRecordMapper = notificationReadRecordMapper;
    }

    @Override
    public IPage<SystemNotificationVO> getNotificationList(Long userId, Page<SystemNotification> page, Integer type) {
        log.info("查询通知列表, userId={}, type={}", userId, type);

        // 查询已发布且目标包含该用户的通知
        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getStatus, 1) // 已发布
                .isNotNull(SystemNotification::getPublishTime)
                .and(wrapper -> wrapper
                        .eq(SystemNotification::getTargetType, 1) // 全部用户
                        .or()
                        .apply("{0} MEMBER OF(target_users)", userId) // 指定用户
                );

        // 如果指定了类型，添加类型过滤
        if (type != null) {
            queryWrapper.eq(SystemNotification::getType, type);
        }

        queryWrapper.orderByDesc(SystemNotification::getPriority)
                .orderByDesc(SystemNotification::getPublishTime);

        IPage<SystemNotification> notificationPage = page(page, queryWrapper);
        return notificationPage.convert(notification -> convertToVO(notification, userId));
    }

    @Override
    public SystemNotificationVO getNotificationDetail(Long notificationId, Long userId) {
        log.info("查询通知详情, notificationId={}, userId={}", notificationId, userId);

        SystemNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "通知不存在");
        }

        // 检查用户是否有权限查看该通知
        if (!canUserView(notification, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此通知");
        }

        return convertToVO(notification, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        log.info("标记通知为已读, notificationId={}, userId={}", notificationId, userId);

        // 检查通知是否存在
        SystemNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "通知不存在");
        }

        // 检查是否已读
        int existingCount = notificationReadRecordMapper.countByNotificationIdAndUserId(notificationId, userId);
        if (existingCount > 0) {
            log.info("通知已标记为已读，无需重复操作");
            return;
        }

        // 插入阅读记录（使用 INSERT IGNORE 避免重复）
        NotificationReadRecord record = new NotificationReadRecord();
        record.setNotificationId(notificationId);
        record.setUserId(userId);
        record.setReadTime(LocalDateTime.now());

        notificationReadRecordMapper.insert(record);
        log.info("标记通知已读成功");
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        log.info("查询未读通知数量, userId={}", userId);

        // 使用 LEFT JOIN 查询未读通知
        // 查询条件：通知已发布 + 目标用户包含当前用户 + 没有阅读记录
        return baseMapper.countUnreadNotifications(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        log.info("标记所有通知为已读, userId={}", userId);

        // 批量插入所有未读通知的阅读记录
        // 使用 INSERT IGNORE 避免重复，单条 SQL 完成批量插入
        baseMapper.batchInsertReadRecords(userId);

        log.info("标记所有通知已读成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllNotifications(Long userId) {
        log.info("清空所有通知, userId={}", userId);

        // 清空通知 = 标记所有通知为已读
        // 这样用户在通知列表中就不会看到这些通知（如果前端过滤已读通知）
        markAllAsRead(userId);

        log.info("清空所有通知成功");
    }

    /**
     * 转换为VO
     */
    private SystemNotificationVO convertToVO(SystemNotification notification, Long userId) {
        SystemNotificationVO vo = new SystemNotificationVO();
        BeanUtil.copyProperties(notification, vo);

        // 设置类型描述
        vo.setTypeDesc(getTypeDesc(notification.getType()));

        // 从关联表查询是否已读
        int readCount = notificationReadRecordMapper.countByNotificationIdAndUserId(
                notification.getNotificationId(), userId);
        vo.setIsRead(readCount > 0);

        return vo;
    }

    /**
     * 检查用户是否有权限查看该通知
     */
    private boolean canUserView(SystemNotification notification, Long userId) {
        // 全部用户都可以查看
        if (notification.getTargetType() == 1) {
            return true;
        }

        // 指定用户
        if (notification.getTargetType() == 2) {
            List<Long> targetUsers = parseUserList(notification.getTargetUsers());
            return targetUsers.contains(userId);
        }

        // 指定等级（暂时不实现，需要用户等级功能）
        return false;
    }

    /**
     * 解析用户ID列表（JSON数组）
     */
    private List<Long> parseUserList(String userListStr) {
        List<Long> users = new ArrayList<>();
        if (StrUtil.isBlank(userListStr)) {
            return users;
        }

        try {
            // 移除JSON数组的方括号并分割
            String content = userListStr.replace("[", "").replace("]", "").replace(" ", "");
            if (StrUtil.isNotBlank(content)) {
                String[] ids = content.split(",");
                for (String id : ids) {
                    if (StrUtil.isNotBlank(id)) {
                        users.add(Long.parseLong(id.trim()));
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析用户ID列表失败: {}", userListStr, e);
        }

        return users;
    }

    /**
     * 获取通知类型描述
     */
    private String getTypeDesc(Integer type) {
        return switch (type) {
            case 1 -> "系统公告";
            case 2 -> "活动通知";
            case 3 -> "账户提醒";
            case 4 -> "交易提醒";
            default -> "未知类型";
        };
    }
}
