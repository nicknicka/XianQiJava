package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserFollow;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserFollowMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.UserFollowService;
import com.xx.xianqijava.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现类
 */
@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    private final UserMapper userMapper;

    public UserFollowServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        // 检查是否尝试关注自己
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }

        // 检查被关注用户是否存在
        User followingUser = userMapper.selectById(followingId);
        if (followingUser == null) {
            throw new BusinessException("被关注用户不存在");
        }

        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        Long count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("已关注该用户");
        }

        // 添加关注关系
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        baseMapper.insert(userFollow);

        log.info("用户关注成功, followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        baseMapper.delete(queryWrapper);

        log.info("用户取消关注成功, followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public int countFollowing(Long followerId) {
        return Math.toIntExact(baseMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
        ));
    }

    @Override
    public int countFollowers(Long followingId) {
        return Math.toIntExact(baseMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowingId, followingId)
        ));
    }

    @Override
    public IPage<UserInfoVO> getFollowingList(Long followerId, Page<UserFollow> page) {
        // 查询关注记录
        IPage<UserFollow> followPage = baseMapper.selectPage(page,
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .orderByDesc(UserFollow::getCreateTime));

        // 转换为UserInfoVO，只返回基本信息
        List<UserInfoVO> userInfoList = followPage.getRecords().stream()
                .map(follow -> {
                    User user = userMapper.selectById(follow.getFollowingId());
                    if (user == null) {
                        return null;
                    }
                    return convertToSimpleUserInfoVO(user);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 构建新的分页结果
        IPage<UserInfoVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), followPage.getTotal());
        resultPage.setRecords(userInfoList);
        return resultPage;
    }

    @Override
    public IPage<UserInfoVO> getFollowerList(Long followingId, Page<UserFollow> page) {
        // 查询粉丝记录
        IPage<UserFollow> followPage = baseMapper.selectPage(page,
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowingId, followingId)
                        .orderByDesc(UserFollow::getCreateTime));

        // 转换为UserInfoVO，只返回基本信息
        List<UserInfoVO> userInfoList = followPage.getRecords().stream()
                .map(follow -> {
                    User user = userMapper.selectById(follow.getFollowerId());
                    if (user == null) {
                        return null;
                    }
                    return convertToSimpleUserInfoVO(user);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 构建新的分页结果
        IPage<UserInfoVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), followPage.getTotal());
        resultPage.setRecords(userInfoList);
        return resultPage;
    }

    /**
     * 转换为简化的用户信息VO（不包含敏感信息）
     */
    private UserInfoVO convertToSimpleUserInfoVO(User user) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getUserId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setCreditScore(user.getCreditScore());
        userInfoVO.setIsVerified(user.getIsVerified());
        userInfoVO.setCollege(user.getCollege());
        userInfoVO.setMajor(user.getMajor());
        return userInfoVO;
    }
}
