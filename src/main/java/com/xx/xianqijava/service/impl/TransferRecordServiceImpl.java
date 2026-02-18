package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.TransferCreateDTO;
import com.xx.xianqijava.dto.TransferRespondDTO;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.entity.ShareItemImage;
import com.xx.xianqijava.entity.TransferRecord;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ShareItemMapper;
import com.xx.xianqijava.mapper.ShareItemImageMapper;
import com.xx.xianqijava.mapper.TransferRecordMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.TransferRecordService;
import com.xx.xianqijava.vo.TransferRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 转赠记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferRecordServiceImpl extends ServiceImpl<TransferRecordMapper, TransferRecord>
        implements TransferRecordService {

    private final ShareItemMapper shareItemMapper;
    private final ShareItemImageMapper shareItemImageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferRecordVO createTransfer(TransferCreateDTO createDTO, Long fromUserId) {
        log.info("发起转赠, shareId={}, toUserId={}, fromUserId={}",
                createDTO.getShareId(), createDTO.getToUserId(), fromUserId);

        // 1. 查询共享物品
        ShareItem shareItem = shareItemMapper.selectById(createDTO.getShareId());
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }

        // 2. 验证权限
        if (!shareItem.getOwnerId().equals(fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限转赠此物品");
        }

        // 3. 验证物品状态（只有可借用或下架状态才能转赠）
        if (shareItem.getStatus() != 0 && shareItem.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "借用中的物品不能转赠");
        }

        // 4. 验证接收人是否存在
        User toUser = userMapper.selectById(createDTO.getToUserId());
        if (toUser == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "接收人不存在");
        }

        // 不能转赠给自己
        if (createDTO.getToUserId().equals(fromUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能转赠给自己");
        }

        // 5. 检查是否有待确认的转赠
        LambdaQueryWrapper<TransferRecord> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(TransferRecord::getShareId, createDTO.getShareId())
                .eq(TransferRecord::getToUserId, createDTO.getToUserId())
                .eq(TransferRecord::getAcceptStatus, 0); // 待确认
        long existCount = count(existWrapper);
        if (existCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该物品已有待确认的转赠请求");
        }

        // 6. 创建转赠记录
        TransferRecord record = new TransferRecord();
        record.setShareId(createDTO.getShareId());
        record.setFromUserId(fromUserId);
        record.setToUserId(createDTO.getToUserId());
        record.setTransferNote(createDTO.getTransferNote());
        record.setAcceptStatus(0); // 待确认

        boolean saved = save(record);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "转赠记录创建失败");
        }

        // TODO: 发送转赠通知给接收人

        log.info("转赠发起成功, transferId={}", record.getTransferId());
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferRecordVO respondTransfer(TransferRespondDTO respondDTO, Long toUserId) {
        log.info("响应转赠, transferId={}, status={}, toUserId={}",
                respondDTO.getTransferId(), respondDTO.getStatus(), toUserId);

        TransferRecord record = getById(respondDTO.getTransferId());
        if (record == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "转赠记录不存在");
        }

        // 验证权限
        if (!record.getToUserId().equals(toUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限响应此转赠");
        }

        // 验证状态
        if (record.getAcceptStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该转赠已处理");
        }

        // 更新转赠记录
        record.setAcceptStatus(respondDTO.getStatus());
        record.setConfirmTime(LocalDateTime.now());

        if (respondDTO.getStatus() == 2) {
            // 拒绝转赠
            record.setRejectReason(respondDTO.getRejectReason());
            updateById(record);
            log.info("转赠已拒绝, transferId={}", record.getTransferId());
            return convertToVO(record);
        }

        // 接受转赠 - 转移物品所有权
        ShareItem shareItem = shareItemMapper.selectById(record.getShareId());
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }

        // 更新物品所有者
        shareItem.setOwnerId(toUserId);
        shareItem.setStatus(1); // 设为可借用状态
        boolean updated = shareItemMapper.updateById(shareItem) > 0;
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "物品转移失败");
        }

        // 标记转赠完成
        record.setCompleteTime(LocalDateTime.now());
        updateById(record);

        // TODO: 发送转赠完成通知给转出人

        log.info("转赠已完成, transferId={}, shareId={}, newOwnerId={}",
                record.getTransferId(), record.getShareId(), toUserId);
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransfer(Long transferId, Long fromUserId) {
        log.info("取消转赠, transferId={}, fromUserId={}", transferId, fromUserId);

        TransferRecord record = getById(transferId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "转赠记录不存在");
        }

        // 验证权限
        if (!record.getFromUserId().equals(fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限取消此转赠");
        }

        // 验证状态（只能取消待确认的转赠）
        if (record.getAcceptStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能取消待确认的转赠");
        }

        boolean removed = removeById(transferId);
        if (!removed) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "取消转赠失败");
        }

        // TODO: 发送取消通知给接收人

        log.info("转赠已取消, transferId={}", transferId);
    }

    @Override
    public TransferRecordVO getTransferRecord(Long transferId) {
        TransferRecord record = getById(transferId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "转赠记录不存在");
        }
        return convertToVO(record);
    }

    @Override
    public IPage<TransferRecordVO> getMySentTransfers(Page<TransferRecord> page, Long fromUserId) {
        log.info("查询我发起的转赠, fromUserId={}, page={}", fromUserId, page.getCurrent());

        LambdaQueryWrapper<TransferRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferRecord::getFromUserId, fromUserId)
                .orderByDesc(TransferRecord::getCreateTime);

        IPage<TransferRecord> recordPage = page(page, wrapper);
        return recordPage.convert(this::convertToVO);
    }

    @Override
    public IPage<TransferRecordVO> getMyReceivedTransfers(Page<TransferRecord> page, Long toUserId) {
        log.info("查询我收到的转赠, toUserId={}, page={}", toUserId, page.getCurrent());

        LambdaQueryWrapper<TransferRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferRecord::getToUserId, toUserId)
                .orderByDesc(TransferRecord::getCreateTime);

        IPage<TransferRecord> recordPage = page(page, wrapper);
        return recordPage.convert(this::convertToVO);
    }

    @Override
    public List<TransferRecordVO> getPendingTransfers(Long toUserId) {
        log.info("查询待确认的转赠, toUserId={}", toUserId);

        LambdaQueryWrapper<TransferRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransferRecord::getToUserId, toUserId)
                .eq(TransferRecord::getAcceptStatus, 0)
                .orderByDesc(TransferRecord::getCreateTime);

        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private TransferRecordVO convertToVO(TransferRecord record) {
        TransferRecordVO vo = new TransferRecordVO();
        BeanUtil.copyProperties(record, vo);

        // 设置状态描述
        vo.setAcceptStatusDesc(getAcceptStatusDesc(record.getAcceptStatus()));

        // 设置时间
        if (record.getCreateTime() != null) {
            vo.setCreateTime(record.getCreateTime().toString());
        }
        if (record.getConfirmTime() != null) {
            vo.setConfirmTime(record.getConfirmTime().toString());
        }
        if (record.getCompleteTime() != null) {
            vo.setCompleteTime(record.getCompleteTime().toString());
        }

        // 获取转出人信息
        User fromUser = userMapper.selectById(record.getFromUserId());
        if (fromUser != null) {
            vo.setFromUserNickname(fromUser.getNickname());
            vo.setFromUserAvatar(fromUser.getAvatar());
        }

        // 获取接收人信息
        User toUser = userMapper.selectById(record.getToUserId());
        if (toUser != null) {
            vo.setToUserNickname(toUser.getNickname());
            vo.setToUserAvatar(toUser.getAvatar());
        }

        // 获取共享物品信息
        ShareItem shareItem = shareItemMapper.selectById(record.getShareId());
        if (shareItem != null) {
            vo.setShareItemTitle(shareItem.getTitle());

            // 获取封面图
            if (shareItem.getCoverImageId() != null) {
                ShareItemImage coverImage = shareItemImageMapper.selectById(shareItem.getCoverImageId());
                if (coverImage != null) {
                    vo.setShareItemImage(coverImage.getImageUrl());
                }
            }
        }

        return vo;
    }

    /**
     * 获取接受状态描述
     */
    private String getAcceptStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待确认";
            case 1:
                return "已接受";
            case 2:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }
}
