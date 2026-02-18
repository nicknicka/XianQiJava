package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.QuickReplyDTO;
import com.xx.xianqijava.entity.QuickReply;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.QuickReplyMapper;
import com.xx.xianqijava.service.QuickReplyService;
import com.xx.xianqijava.vo.QuickReplyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 快捷回复服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuickReplyServiceImpl extends ServiceImpl<QuickReplyMapper, QuickReply> implements QuickReplyService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuickReplyVO createQuickReply(QuickReplyDTO dto, Long userId) {
        log.info("创建快捷回复, userId={}, title={}", userId, dto.getTitle());

        // 检查用户创建的快捷回复数量限制（最多20个）
        LambdaQueryWrapper<QuickReply> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(QuickReply::getUserId, userId);
        long count = count(countWrapper);
        if (count >= 20) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "个人快捷回复最多创建20个");
        }

        // 创建快捷回复
        QuickReply quickReply = new QuickReply();
        quickReply.setUserId(userId);
        quickReply.setTitle(dto.getTitle());
        quickReply.setContent(dto.getContent());
        quickReply.setCategory(dto.getCategory());
        quickReply.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        quickReply.setIsSystem(0); // 用户自定义

        save(quickReply);
        log.info("快捷回复创建成功, replyId={}", quickReply.getReplyId());

        return convertToVO(quickReply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuickReply(Long replyId, QuickReplyDTO dto, Long userId) {
        log.info("更新快捷回复, replyId={}, userId={}", replyId, userId);

        QuickReply quickReply = getById(replyId);
        if (quickReply == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "快捷回复不存在");
        }

        // 检查权限：只能修改自己的快捷回复
        if (!quickReply.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能修改自己的快捷回复");
        }

        // 系统预设的快捷回复不能修改
        if (quickReply.getIsSystem() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统预设快捷回复不能修改");
        }

        // 更新快捷回复
        quickReply.setTitle(dto.getTitle());
        quickReply.setContent(dto.getContent());
        quickReply.setCategory(dto.getCategory());
        if (dto.getSortOrder() != null) {
            quickReply.setSortOrder(dto.getSortOrder());
        }

        updateById(quickReply);
        log.info("快捷回复更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuickReply(Long replyId, Long userId) {
        log.info("删除快捷回复, replyId={}, userId={}", replyId, userId);

        QuickReply quickReply = getById(replyId);
        if (quickReply == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "快捷回复不存在");
        }

        // 检查权限：只能删除自己的快捷回复
        if (!quickReply.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己的快捷回复");
        }

        // 系统预设的快捷回复不能删除
        if (quickReply.getIsSystem() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统预设快捷回复不能删除");
        }

        removeById(replyId);
        log.info("快捷回复删除成功");
    }

    @Override
    public IPage<QuickReplyVO> getQuickReplyList(Long userId, Page<QuickReply> page) {
        log.info("查询快捷回复列表, userId={}", userId);

        // 查询系统预设和用户自定义的快捷回复
        LambdaQueryWrapper<QuickReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .eq(QuickReply::getUserId, 0) // 系统预设
                        .or()
                        .eq(QuickReply::getUserId, userId) // 用户自定义
                )
                .orderByAsc(QuickReply::getIsSystem) // 系统预设排在前面
                .orderByAsc(QuickReply::getSortOrder)
                .orderByDesc(QuickReply::getCreateTime);

        IPage<QuickReply> quickReplyPage = page(page, queryWrapper);
        return quickReplyPage.convert(this::convertToVO);
    }

    @Override
    public List<QuickReplyVO> getSystemQuickReplies() {
        log.info("查询系统预设快捷回复");

        LambdaQueryWrapper<QuickReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuickReply::getUserId, 0)
                .eq(QuickReply::getIsSystem, 1)
                .orderByAsc(QuickReply::getSortOrder);

        List<QuickReply> quickReplies = list(queryWrapper);
        return quickReplies.stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 转换为VO
     */
    private QuickReplyVO convertToVO(QuickReply quickReply) {
        QuickReplyVO vo = new QuickReplyVO();
        BeanUtil.copyProperties(quickReply, vo);
        return vo;
    }
}
