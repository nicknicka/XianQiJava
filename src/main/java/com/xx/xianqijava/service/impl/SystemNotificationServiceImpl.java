package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.SystemNotification;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.SystemNotificationMapper;
import com.xx.xianqijava.service.SystemNotificationService;
import com.xx.xianqijava.vo.SystemNotificationVO;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification>
        implements SystemNotificationService {

    @Override
    public IPage<SystemNotificationVO> getNotificationList(Long userId, Page<SystemNotification> page) {
        log.info("查询通知列表, userId={}", userId);

        // 查询已发布且目标包含该用户的通知
        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getStatus, 1) // 已发布
                .isNotNull(SystemNotification::getPublishTime)
                .and(wrapper -> wrapper
                        .eq(SystemNotification::getTargetType, 1) // 全部用户
                        .or()
                        .apply("FIND_IN_SET({0}, target_users)", userId) // 指定用户
                )
                .orderByDesc(SystemNotification::getPriority)
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

        SystemNotification notification = getById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "通知不存在");
        }

        // 使用SQL级别的更新避免并发问题
        // 先检查是否已读
        List<Long> readUsers = parseUserList(notification.getIsRead());
        if (readUsers.contains(userId)) {
            log.info("通知已标记为已读，无需重复操作");
            return;
        }

        // 使用FIND_IN_SET和CONCAT避免并发丢失更新
        String updateSql = String.format(
            "is_read = CASE " +
            "WHEN is_read IS NULL THEN '[%d]' " +
            "WHEN FIND_IN_SET(%d, is_read) = 0 THEN CONCAT(is_read, ',%d') " +
            "ELSE is_read END",
            userId, userId, userId
        );

        // 使用LambdaUpdateWrapper进行条件更新
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SystemNotification> updateWrapper =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.setSql(true, updateSql)
                .eq(SystemNotification::getNotificationId, notificationId);

        int updated = baseMapper.update(null, updateWrapper);
        if (updated > 0) {
            log.info("标记通知已读成功");
        } else {
            log.warn("标记通知已读失败，可能已被其他操作更新");
        }
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        log.info("查询未读通知数量, userId={}", userId);

        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getStatus, 1)
                .eq(SystemNotification::getPublishTime).isNotNull()
                .and(wrapper -> wrapper
                        .eq(SystemNotification::getTargetType, 1)
                        .or()
                        .apply("FIND_IN_SET({0}, target_users)", userId)
                )
                .apply("NOT FIND_IN_SET({0}, is_read)", userId);

        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        log.info("标记所有通知为已读, userId={}", userId);

        // 查询用户所有未读的通知
        LambdaQueryWrapper<SystemNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemNotification::getStatus, 1)
                .eq(SystemNotification::getPublishTime).isNotNull()
                .and(wrapper -> wrapper
                        .eq(SystemNotification::getTargetType, 1)
                        .or()
                        .apply("FIND_IN_SET({0}, target_users)", userId)
                )
                .apply("NOT FIND_IN_SET({0}, is_read)", userId);

        List<SystemNotification> notifications = list(queryWrapper);

        // 标记所有未读通知为已读
        for (SystemNotification notification : notifications) {
            List<Long> readUsers = parseUserList(notification.getIsRead());
            if (!readUsers.contains(userId)) {
                readUsers.add(userId);
                notification.setIsRead(formatUserList(readUsers));
            }
        }

        updateBatchById(notifications);
        log.info("标记所有通知已读成功, count={}", notifications.size());
    }

    /**
     * 转换为VO
     */
    private SystemNotificationVO convertToVO(SystemNotification notification, Long userId) {
        SystemNotificationVO vo = new SystemNotificationVO();
        BeanUtil.copyProperties(notification, vo);

        // 设置类型描述
        vo.setTypeDesc(getTypeDesc(notification.getType()));

        // 设置是否已读
        List<Long> readUsers = parseUserList(notification.getIsRead());
        vo.setIsRead(readUsers.contains(userId));

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
     * 格式化用户ID列表为JSON数组字符串
     */
    private String formatUserList(List<Long> users) {
        if (users == null || users.isEmpty()) {
            return null;
        }
        return users.toString();
    }

    /**
     * 获取通知类型描述
     */
    private String getTypeDesc(Integer type) {
        switch (type) {
            case 1:
                return "系统公告";
            case 2:
                return "活动通知";
            case 3:
                return "账户提醒";
            case 4:
                return "交易提醒";
            default:
                return "未知类型";
        }
    }
}
