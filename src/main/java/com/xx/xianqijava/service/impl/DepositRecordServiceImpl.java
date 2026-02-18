package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.DepositPayDTO;
import com.xx.xianqijava.dto.DepositRefundDTO;
import com.xx.xianqijava.entity.DepositRecord;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.entity.ShareItemBooking;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.DepositRecordMapper;
import com.xx.xianqijava.mapper.ShareItemBookingMapper;
import com.xx.xianqijava.mapper.ShareItemMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.DepositRecordService;
import com.xx.xianqijava.vo.DepositRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 押金记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositRecordServiceImpl extends ServiceImpl<DepositRecordMapper, DepositRecord>
        implements DepositRecordService {

    private final ShareItemBookingMapper shareItemBookingMapper;
    private final ShareItemMapper shareItemMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepositRecordVO payDeposit(DepositPayDTO payDTO, Long userId) {
        log.info("支付押金, userId={}, bookingId={}", userId, payDTO.getBookingId());

        // 1. 查询预约记录
        ShareItemBooking booking = shareItemBookingMapper.selectById(payDTO.getBookingId());
        if (booking == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约记录不存在");
        }

        // 2. 验证权限
        if (!booking.getBorrowerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限支付此押金");
        }

        // 3. 验证预约状态
        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只有已批准的预约才能支付押金");
        }

        // 4. 检查是否已支付
        LambdaQueryWrapper<DepositRecord> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(DepositRecord::getBookingId, payDTO.getBookingId())
                .eq(DepositRecord::getUserId, userId)
                .ne(DepositRecord::getStatus, 0);
        long existCount = count(existWrapper);
        if (existCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该预约已支付押金");
        }

        // 5. 创建押金记录
        DepositRecord record = new DepositRecord();
        record.setBookingId(payDTO.getBookingId());
        record.setShareId(booking.getShareId());
        record.setUserId(userId);
        record.setAmount(booking.getDeposit());
        record.setPaymentMethod(payDTO.getPaymentMethod());
        record.setTransactionNo(generateTransactionNo());
        record.setStatus(0); // 待支付

        boolean saved = save(record);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "押金记录创建失败");
        }

        // TODO: 调用第三方支付接口
        // 这里模拟支付成功
        record.setStatus(1); // 已支付
        updateById(record);

        log.info("押金支付成功, recordId={}", record.getRecordId());
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepositRecordVO refundDeposit(DepositRefundDTO refundDTO, Long ownerId) {
        log.info("退还押金, recordId={}, ownerId={}", refundDTO.getRecordId(), ownerId);

        DepositRecord record = getById(refundDTO.getRecordId());
        if (record == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "押金记录不存在");
        }

        // 验证状态
        if (record.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能退还已支付的押金");
        }

        // 验证权限（物品所有者才能退还）
        ShareItem shareItem = shareItemMapper.selectById(record.getShareId());
        if (shareItem == null || !shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限退还此押金");
        }

        // 验证借用是否已完成
        ShareItemBooking booking = shareItemBookingMapper.selectById(record.getBookingId());
        if (booking == null || booking.getStatus() != 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能退还已完成订单的押金");
        }

        // 更新押金记录
        record.setStatus(2); // 已退还
        record.setRefundTime(java.time.LocalDateTime.now());
        record.setRefundRemark(refundDTO.getDeductReason());

        boolean updated = updateById(record);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退还押金失败");
        }

        // TODO: 调用第三方支付接口进行退款

        log.info("押金退还成功, recordId={}", record.getRecordId());
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductDeposit(Long recordId, String deductReason, Long ownerId) {
        log.info("扣除押金, recordId={}, ownerId={}, reason={}", recordId, ownerId, deductReason);

        DepositRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "押金记录不存在");
        }

        // 验证状态
        if (record.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能扣除已支付的押金");
        }

        // 验证权限
        ShareItem shareItem = shareItemMapper.selectById(record.getShareId());
        if (shareItem == null || !shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限扣除此押金");
        }

        // 更新押金记录
        record.setStatus(3); // 已扣除
        record.setDeductReason(deductReason);

        boolean updated = updateById(record);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "扣除押金失败");
        }

        log.info("押金扣除成功, recordId={}", recordId);
    }

    @Override
    public DepositRecordVO getDepositRecord(Long recordId) {
        DepositRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "押金记录不存在");
        }
        return convertToVO(record);
    }

    @Override
    public IPage<DepositRecordVO> getMyDepositRecords(Page<DepositRecord> page, Long userId) {
        log.info("查询我的押金记录, userId={}, page={}", userId, page.getCurrent());

        LambdaQueryWrapper<DepositRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepositRecord::getUserId, userId)
                .orderByDesc(DepositRecord::getCreateTime);

        IPage<DepositRecord> recordPage = page(page, wrapper);
        return recordPage.convert(this::convertToVO);
    }

    @Override
    public DepositRecordVO getDepositByBookingId(Long bookingId) {
        LambdaQueryWrapper<DepositRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepositRecord::getBookingId, bookingId)
                .orderByDesc(DepositRecord::getCreateTime)
                .last("LIMIT 1");

        DepositRecord record = getOne(wrapper);
        if (record == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未找到该预约的押金记录");
        }
        return convertToVO(record);
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo() {
        return "DEP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 转换为VO
     */
    private DepositRecordVO convertToVO(DepositRecord record) {
        DepositRecordVO vo = new DepositRecordVO();
        BeanUtil.copyProperties(record, vo);

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(record.getStatus()));

        // 设置支付方式描述
        vo.setPaymentMethodDesc(getPaymentMethodDesc(record.getPaymentMethod()));

        // 设置时间
        if (record.getCreateTime() != null) {
            vo.setCreateTime(record.getCreateTime().toString());
        }
        if (record.getRefundTime() != null) {
            vo.setRefundTime(record.getRefundTime().toString());
        }

        // 获取用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
        }

        // 获取共享物品信息
        ShareItem shareItem = shareItemMapper.selectById(record.getShareId());
        if (shareItem != null) {
            vo.setShareItemTitle(shareItem.getTitle());
        }

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "已支付";
            case 2:
                return "已退还";
            case 3:
                return "已扣除";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取支付方式描述
     */
    private String getPaymentMethodDesc(Integer paymentMethod) {
        switch (paymentMethod) {
            case 1:
                return "余额";
            case 2:
                return "支付宝";
            case 3:
                return "微信";
            default:
                return "未知";
        }
    }
}
