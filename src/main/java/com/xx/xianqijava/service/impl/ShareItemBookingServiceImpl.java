package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.BookingApproveDTO;
import com.xx.xianqijava.dto.BookingReturnDTO;
import com.xx.xianqijava.dto.ShareItemBookingCreateDTO;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.entity.ShareItemBooking;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ShareItemBookingMapper;
import com.xx.xianqijava.mapper.ShareItemMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ShareItemBookingService;
import com.xx.xianqijava.vo.ShareItemBookingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 共享物品预约借用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareItemBookingServiceImpl extends ServiceImpl<ShareItemBookingMapper, ShareItemBooking>
        implements ShareItemBookingService {

    private final ShareItemMapper shareItemMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareItemBookingVO createBooking(ShareItemBookingCreateDTO createDTO, Long borrowerId) {
        log.info("创建预约借用, borrowerId={}, shareId={}", borrowerId, createDTO.getShareId());

        // 1. 查询共享物品
        ShareItem shareItem = shareItemMapper.selectById(createDTO.getShareId());
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }

        // 2. 检查物品状态
        if (shareItem.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "物品当前不可借用");
        }

        // 3. 不能借用自己的物品
        if (shareItem.getOwnerId().equals(borrowerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能借用自己发布的物品");
        }

        // 4. 验证借用日期
        LocalDate startDate = createDTO.getStartDate();
        LocalDate endDate = createDTO.getEndDate();

        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "借用开始日期不能早于今天");
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "借用结束日期不能早于开始日期");
        }

        // 5. 检查日期范围是否已有预约
        LambdaQueryWrapper<ShareItemBooking> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ShareItemBooking::getShareId, createDTO.getShareId())
                .in(ShareItemBooking::getStatus, 0, 1, 4) // 待审批、已批准、借用中
                .and(wrapper -> wrapper
                        .ge(ShareItemBooking::getEndDate, startDate)
                        .le(ShareItemBooking::getStartDate, endDate));

        long conflictCount = count(checkWrapper);
        if (conflictCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该时间段已被预约，请选择其他时间");
        }

        // 6. 计算借用天数和费用
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal dailyRent = shareItem.getDailyRent();
        BigDecimal deposit = shareItem.getDeposit();
        BigDecimal totalRent = dailyRent.multiply(BigDecimal.valueOf(days));
        BigDecimal totalAmount = totalRent.add(deposit);

        // 7. 创建预约记录
        ShareItemBooking booking = new ShareItemBooking();
        booking.setShareId(createDTO.getShareId());
        booking.setOwnerId(shareItem.getOwnerId());
        booking.setBorrowerId(borrowerId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setDays((int) days);
        booking.setTotalRent(totalRent);
        booking.setDeposit(deposit);
        booking.setTotalAmount(totalAmount);
        booking.setRemark(createDTO.getRemark());
        booking.setStatus(0); // 待审批
        booking.setDepositReturned(0);

        boolean saved = save(booking);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "预约失败");
        }

        log.info("预约借用创建成功, bookingId={}", booking.getBookingId());
        return convertToVO(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareItemBookingVO approveBooking(BookingApproveDTO approveDTO, Long ownerId) {
        log.info("审批预约, bookingId={}, status={}, ownerId={}",
                approveDTO.getBookingId(), approveDTO.getStatus(), ownerId);

        ShareItemBooking booking = getById(approveDTO.getBookingId());
        if (booking == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约记录不存在");
        }

        // 验证权限
        if (!booking.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限审批此预约");
        }

        // 验证状态
        if (booking.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该预约已被处理");
        }

        int status = approveDTO.getStatus();
        if (status != 1 && status != 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的审批状态");
        }

        // 更新预约状态
        booking.setStatus(status);
        booking.setApproveRemark(approveDTO.getApproveRemark());
        booking.setApproveTime(java.time.LocalDateTime.now());

        boolean updated = updateById(booking);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "审批失败");
        }

        // 如果批准，更新共享物品状态为借用中
        if (status == 1) {
            ShareItem shareItem = shareItemMapper.selectById(booking.getShareId());
            if (shareItem != null) {
                shareItem.setStatus(2); // 借用中
                shareItemMapper.updateById(shareItem);
            }
        }

        log.info("预约审批成功, bookingId={}, status={}", booking.getBookingId(), status);
        return convertToVO(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId, Long borrowerId) {
        log.info("取消预约, bookingId={}, borrowerId={}", bookingId, borrowerId);

        ShareItemBooking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约记录不存在");
        }

        // 验证权限
        if (!booking.getBorrowerId().equals(borrowerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限取消此预约");
        }

        // 只有待审批和已批准的预约可以取消
        if (booking.getStatus() != 0 && booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该预约当前状态不允许取消");
        }

        // 保存原始状态用于判断是否需要恢复物品
        Integer originalStatus = booking.getStatus();

        // 更新预约状态为已取消
        booking.setStatus(3); // 已取消
        boolean updated = updateById(booking);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "取消预约失败");
        }

        // 如果是已批准状态，需要恢复物品状态
        if (originalStatus == 1) {
            ShareItem shareItem = shareItemMapper.selectById(booking.getShareId());
            if (shareItem != null && shareItem.getStatus() == 2) {
                shareItem.setStatus(1); // 恢复为可借用
                shareItemMapper.updateById(shareItem);
                log.info("恢复物品状态为可借用, shareId={}", shareItem.getShareId());
            }
        }

        log.info("预约取消成功, bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareItemBookingVO confirmReturn(BookingReturnDTO returnDTO, Long ownerId) {
        log.info("确认归还, bookingId={}, ownerId={}", returnDTO.getBookingId(), ownerId);

        ShareItemBooking booking = getById(returnDTO.getBookingId());
        if (booking == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约记录不存在");
        }

        // 验证权限
        if (!booking.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限确认归还");
        }

        // 验证状态
        if (booking.getStatus() != 4) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该预约状态不是借用中");
        }

        // 更新预约状态
        booking.setStatus(5); // 已完成
        booking.setReturnTime(java.time.LocalDateTime.now());
        booking.setReturnRemark(returnDTO.getReturnRemark());

        boolean updated = updateById(booking);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "确认归还失败");
        }

        // 恢复共享物品状态为可借用
        ShareItem shareItem = shareItemMapper.selectById(booking.getShareId());
        if (shareItem != null) {
            shareItem.setStatus(1); // 可借用
            shareItemMapper.updateById(shareItem);
        }

        log.info("确认归还成功, bookingId={}", booking.getBookingId());
        return convertToVO(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnDeposit(Long bookingId, Long ownerId) {
        log.info("退还押金, bookingId={}, ownerId={}", bookingId, ownerId);

        ShareItemBooking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约记录不存在");
        }

        // 验证权限
        if (!booking.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限退还押金");
        }

        // 验证状态
        if (booking.getStatus() != 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能退还已完成订单的押金");
        }

        // 检查是否已退还
        if (booking.getDepositReturned() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "押金已退还");
        }

        // 更新退还状态
        booking.setDepositReturned(1);
        booking.setDepositReturnTime(java.time.LocalDateTime.now());

        boolean updated = updateById(booking);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退还押金失败");
        }

        log.info("押金退还成功, bookingId={}", bookingId);
    }

    @Override
    public ShareItemBookingVO getBookingDetail(Long bookingId) {
        ShareItemBooking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约记录不存在");
        }
        return convertToVO(booking);
    }

    @Override
    public IPage<ShareItemBookingVO> getMyBookings(Page<ShareItemBooking> page, Long borrowerId) {
        log.info("查询我的预约列表, borrowerId={}, page={}", borrowerId, page.getCurrent());

        LambdaQueryWrapper<ShareItemBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareItemBooking::getBorrowerId, borrowerId)
                .orderByDesc(ShareItemBooking::getCreateTime);

        IPage<ShareItemBooking> bookingPage = page(page, wrapper);
        return bookingPage.convert(this::convertToVO);
    }

    @Override
    public IPage<ShareItemBookingVO> getReceivedBookings(Page<ShareItemBooking> page, Long ownerId) {
        log.info("查询收到的预约列表, ownerId={}, page={}", ownerId, page.getCurrent());

        LambdaQueryWrapper<ShareItemBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareItemBooking::getOwnerId, ownerId)
                .orderByDesc(ShareItemBooking::getCreateTime);

        IPage<ShareItemBooking> bookingPage = page(page, wrapper);
        return bookingPage.convert(this::convertToVO);
    }

    /**
     * 转换为VO
     */
    private ShareItemBookingVO convertToVO(ShareItemBooking booking) {
        ShareItemBookingVO vo = new ShareItemBookingVO();
        BeanUtil.copyProperties(booking, vo);

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(booking.getStatus()));

        // 设置时间
        if (booking.getCreateTime() != null) {
            vo.setCreateTime(booking.getCreateTime().toString());
        }
        if (booking.getApproveTime() != null) {
            vo.setApproveTime(booking.getApproveTime().toString());
        }
        if (booking.getReturnTime() != null) {
            vo.setReturnTime(booking.getReturnTime().toString());
        }
        if (booking.getDepositReturnTime() != null) {
            vo.setDepositReturnTime(booking.getDepositReturnTime().toString());
        }

        // 获取共享物品信息
        ShareItem shareItem = shareItemMapper.selectById(booking.getShareId());
        if (shareItem != null) {
            vo.setShareItemTitle(shareItem.getTitle());
            vo.setDailyRent(shareItem.getDailyRent());

            // 获取封面图
            LambdaQueryWrapper<com.xx.xianqijava.entity.ShareItemImage> imageWrapper =
                new LambdaQueryWrapper<>();
            imageWrapper.eq(com.xx.xianqijava.entity.ShareItemImage::getShareId, booking.getShareId())
                    .eq(com.xx.xianqijava.entity.ShareItemImage::getIsCover, 1)
                    .last("LIMIT 1");
            // 需要注入 ShareItemImageMapper，这里简化处理
        }

        // 获取所有者信息
        User owner = userMapper.selectById(booking.getOwnerId());
        if (owner != null) {
            vo.setOwnerNickname(owner.getNickname());
            vo.setOwnerAvatar(owner.getAvatar());
        }

        // 获取借用者信息
        User borrower = userMapper.selectById(booking.getBorrowerId());
        if (borrower != null) {
            vo.setBorrowerNickname(borrower.getNickname());
            vo.setBorrowerAvatar(borrower.getAvatar());
        }

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待审批";
            case 1:
                return "已批准";
            case 2:
                return "已拒绝";
            case 3:
                return "已取消";
            case 4:
                return "借用中";
            case 5:
                return "已完成";
            default:
                return "未知状态";
        }
    }
}
