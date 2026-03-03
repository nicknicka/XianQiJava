package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.UserFollow;
import com.xx.xianqijava.vo.UserInfoVO;

/**
 * 用户关注服务接口
 */
public interface UserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     */
    void followUser(Long followerId, Long followingId);

    /**
     * 取消关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     */
    void unfollowUser(Long followerId, Long followingId);

    /**
     * 检查是否已关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 统计用户的关注数量
     *
     * @param followerId 关注者ID
     * @return 关注数量
     */
    int countFollowing(Long followerId);

    /**
     * 统计用户的粉丝数量
     *
     * @param followingId 被关注者ID
     * @return 粉丝数量
     */
    int countFollowers(Long followingId);

    /**
     * 获取用户的关注列表（分页）
     *
     * @param followerId 关注者ID
     * @param page       分页参数
     * @return 关注的用户列表
     */
    IPage<UserInfoVO> getFollowingList(Long followerId, Page<UserFollow> page);

    /**
     * 获取用户的粉丝列表（分页）
     *
     * @param followingId 被关注者ID
     * @param page        分页参数
     * @return 粉丝列表
     */
    IPage<UserInfoVO> getFollowerList(Long followingId, Page<UserFollow> page);
}
