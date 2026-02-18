package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.SystemNotification;
import com.xx.xianqijava.vo.SystemNotificationVO;

/**
 * 系统通知服务接口
 */
public interface SystemNotificationService extends IService<SystemNotification> {

    /**
     * 获取通知列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 通知列表
     */
    IPage<SystemNotificationVO> getNotificationList(Long userId, Page<SystemNotification> page);

    /**
     * 获取通知详情
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 通知详情
     */
    SystemNotificationVO getNotificationDetail(Long notificationId, Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);
}
