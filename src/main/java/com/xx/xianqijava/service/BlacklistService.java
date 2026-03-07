package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.Blacklist;
import com.xx.xianqijava.vo.UserInfoVO;

/**
 * 黑名单服务接口
 */
public interface BlacklistService extends IService<Blacklist> {

    /**
     * 添加黑名单
     *
     * @param userId       用户ID
     * @param blockedUserId 被拉黑的用户ID
     * @param reason       拉黑原因
     */
    void addToBlacklist(Long userId, Long blockedUserId, String reason);

    /**
     * 移除黑名单
     *
     * @param userId         用户ID
     * @param blacklistId 黑名单ID
     */
    void removeFromBlacklist(Long userId, Long blacklistId);

    /**
     * 通过被拉黑用户ID移除黑名单
     *
     * @param userId         用户ID
     * @param blockedUserId 被拉黑的用户ID
     */
    void removeFromBlacklistByBlockedUserId(Long userId, Long blockedUserId);

    /**
     * 获取黑名单列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 黑名单列表
     */
    IPage<UserInfoVO> getBlacklist(Long userId, Page<Blacklist> page);

    /**
     * 检查用户是否在黑名单中
     *
     * @param userId       用户ID
     * @param targetUserId 目标用户ID
     * @return 是否在黑名单中
     */
    boolean isInBlacklist(Long userId, Long targetUserId);

    /**
     * 检查当前用户是否被对方拉黑
     *
     * @param userId       当前用户ID
     * @param targetUserId 对方用户ID
     * @return 是否被对方拉黑
     */
    boolean isBlockedBy(Long userId, Long targetUserId);
}
