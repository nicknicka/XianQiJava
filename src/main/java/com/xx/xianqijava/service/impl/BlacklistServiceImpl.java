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
    public IPage<UserInfoVO> getBlacklist(Long userId, Page<Blacklist> page) {
        log.info("查询黑名单列表, userId={}", userId);

        // 查询黑名单列表
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getUserId, userId)
                .orderByDesc(Blacklist::getCreateTime);

        IPage<Blacklist> blacklistPage = page(page, queryWrapper);

        // 转换为用户信息VO
        return blacklistPage.convert(blacklist -> {
            User blockedUser = userMapper.selectById(blacklist.getBlockedUserId());
            if (blockedUser == null) {
                return null;
            }

            UserInfoVO vo = new UserInfoVO();
            vo.setUserId(blockedUser.getUserId());
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
        });
    }

    @Override
    public boolean isInBlacklist(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getUserId, userId)
                .eq(Blacklist::getBlockedUserId, targetUserId);
        return count(queryWrapper) > 0;
    }
}
