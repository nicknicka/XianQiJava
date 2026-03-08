package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.Blacklist;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.BlacklistMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.BlacklistService;
import com.xx.xianqijava.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 黑名单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToBlacklist(Long userId, Long blockedUserId, String reason) {
        log.info("添加黑名单, userId={}, blockedUserId={}", userId, blockedUserId);

        // 不能将自己拉黑
        if (userId.equals(blockedUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能将自己拉黑");
        }

        // 检查被拉黑用户是否存在
        User blockedUser = userMapper.selectById(blockedUserId);
        if (blockedUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "被拉黑的用户不存在");
        }

        // 检查是否已经在黑名单中
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getUserId, userId)
                .eq(Blacklist::getBlockedUserId, blockedUserId);
        if (count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该用户已在黑名单中");
        }

        // 添加到黑名单
        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(userId);
        blacklist.setBlockedUserId(blockedUserId);
        blacklist.setReason(reason);

        save(blacklist);
        log.info("添加黑名单成功, blacklistId={}", blacklist.getBlacklistId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklist(Long userId, Long blacklistId) {
        log.info("移除黑名单, userId={}, blacklistId={}", userId, blacklistId);

        Blacklist blacklist = getById(blacklistId);
        if (blacklist == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "黑名单记录不存在");
        }

        // 检查权限：只有黑名单的拥有者可以移除
        if (!blacklist.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权移除此黑名单记录");
        }

        removeById(blacklistId);
        log.info("移除黑名单成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklistByBlockedUserId(Long userId, Long blockedUserId) {
        log.info("通过被拉黑用户ID移除黑名单, userId={}, blockedUserId={}", userId, blockedUserId);

        // 查找黑名单记录
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getUserId, userId)
                .eq(Blacklist::getBlockedUserId, blockedUserId);

        Blacklist blacklist = getOne(queryWrapper);
        if (blacklist == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "黑名单记录不存在");
        }

        // 删除黑名单记录
        removeById(blacklist.getBlacklistId());
        log.info("通过被拉黑用户ID移除黑名单成功, blacklistId={}", blacklist.getBlacklistId());
    }

    @Override
    public IPage<UserInfoVO> getBlacklist(Long userId, Page<Blacklist> page) {
        log.info("查询黑名单列表, userId={}", userId);

        // 查询黑名单列表
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getUserId, userId)
                .orderByDesc(Blacklist::getCreateTime);

        IPage<Blacklist> blacklistPage = page(page, queryWrapper);

        // 转换为用户信息VO，过滤掉已删除的用户
        java.util.List<UserInfoVO> validUsers = blacklistPage.getRecords().stream()
                .map(blacklist -> {
                    User blockedUser = userMapper.selectById(blacklist.getBlockedUserId());
                    if (blockedUser == null || blockedUser.getDeleted() == 1) {
                        return null;
                    }

                    UserInfoVO vo = new UserInfoVO();
                    vo.setId(blockedUser.getUserId());
                    vo.setUsername(blockedUser.getUsername());
                    vo.setNickname(blockedUser.getNickname());
                    vo.setAvatar(blockedUser.getAvatar());
                    vo.setStudentId(blockedUser.getStudentId());
                    vo.setCollege(blockedUser.getCollege());
                    vo.setMajor(blockedUser.getMajor());
                    vo.setCreditScore(blockedUser.getCreditScore());
                    vo.setStatus(blockedUser.getStatus());
                    vo.setIsVerified(blockedUser.getIsVerified());

                    return vo;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        // 构建新的分页结果
        IPage<UserInfoVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), blacklistPage.getTotal());
        resultPage.setRecords(validUsers);
        return resultPage;
    }

    @Override
    public boolean isInBlacklist(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getUserId, userId)
                .eq(Blacklist::getBlockedUserId, targetUserId);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean isBlockedBy(Long userId, Long targetUserId) {
        // 检查对方是否将当前用户拉黑（即对方的黑名单中是否有当前用户）
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getUserId, targetUserId)
                .eq(Blacklist::getBlockedUserId, userId);
        return count(queryWrapper) > 0;
    }

    @Override
    public IPage<Blacklist> getBlacklistList(Page<Blacklist> page, Long userId, Long blockedUserId,
                                             String keyword, String startTime, String endTime) {
        log.info("查询黑名单列表（管理员）, userId={}, blockedUserId={}, keyword={}",
                userId, blockedUserId, keyword);

        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();

        // 按用户ID筛选
        if (userId != null) {
            queryWrapper.eq(Blacklist::getUserId, userId);
        }

        // 按被拉黑用户ID筛选
        if (blockedUserId != null) {
            queryWrapper.eq(Blacklist::getBlockedUserId, blockedUserId);
        }

        // 按时间范围筛选
        if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
            queryWrapper.between(Blacklist::getCreateTime, startTime, endTime);
        }

        // 按关键词筛选（需要在用户表中搜索昵称或手机号）
        // 关键词搜索暂时不支持，因为需要JOIN用户表
        // 如果需要支持，可以改用自定义SQL或QueryWrapper的嵌套查询

        queryWrapper.orderByDesc(Blacklist::getCreateTime);

        return page(page, queryWrapper);
    }
}
