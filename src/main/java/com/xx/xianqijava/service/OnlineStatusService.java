package com.xx.xianqijava.service;

import java.util.Set;

/**
 * 在线状态管理服务接口
 *
 * @author Claude Code
 * @since 2026-03-08
 */
public interface OnlineStatusService {

    /**
     * 用户上线
     *
     * @param userId 用户ID
     */
    void userOnline(Long userId);

    /**
     * 用户下线
     *
     * @param userId 用户ID
     */
    void userOffline(Long userId);

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 获取所有在线用户ID
     *
     * @return 在线用户ID集合
     */
    Set<Long> getOnlineUserIds();

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    int getOnlineUserCount();

    /**
     * 批量检查用户在线状态
     *
     * @param userIds 用户ID列表
     * @return 在线状态映射（用户ID -> 是否在线）
     */
    java.util.Map<Long, Boolean> checkUsersOnline(Iterable<Long> userIds);
}
