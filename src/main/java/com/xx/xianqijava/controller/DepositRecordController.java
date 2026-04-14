package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.entity.ShareItemBooking;
import com.xx.xianqijava.entity.ShareItemImage;
import com.xx.xianqijava.mapper.ShareItemBookingMapper;
import com.xx.xianqijava.mapper.ShareItemImageMapper;
import com.xx.xianqijava.mapper.ShareItemMapper;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.DepositRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 押金管理控制器
 * 注意：押金信息实际存储在 share_item_booking 表中
 */
@Slf4j
@Tag(name = "押金管理")
@RestController
@RequestMapping("/deposit")
@RequiredArgsConstructor
public class DepositRecordController {

    private final ShareItemBookingMapper shareItemBookingMapper;
    private final ShareItemMapper shareItemMapper;
    private final ShareItemImageMapper shareItemImageMapper;

    /**
     * 获取我的押金记录列表
     * 从 share_item_booking 表查询押金信息
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的押金记录列表")
    public Result<IPage<DepositRecordVO>> getMyDepositRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的押金记录列表, userId={}, page={}", userId, page);

        // 从 share_item_booking 表查询用户的预约记录（包含押金信息）
        Page<ShareItemBooking> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ShareItemBooking> queryWrapper = new LambdaQueryWrapper<ShareItemBooking>()
                .eq(ShareItemBooking::getBorrowerId, userId)
                .gt(ShareItemBooking::getDeposit, BigDecimal.ZERO)
                .orderByDesc(ShareItemBooking::getCreateTime);

        IPage<ShareItemBooking> bookingPage = shareItemBookingMapper.selectPage(pageParam, queryWrapper);

        // 转换为 DepositRecordVO 格式
        IPage<DepositRecordVO> recordPage = new Page<>(page, size);
        recordPage.setTotal(bookingPage.getTotal());
        recordPage.setCurrent(bookingPage.getCurrent());
        recordPage.setSize(bookingPage.getSize());

        List<DepositRecordVO> records = bookingPage.getRecords().stream()
                .map(booking -> {
                    ShareItem shareItem = shareItemMapper.selectById(booking.getShareId());

                    DepositRecordVO vo = new DepositRecordVO();
                    vo.setRecordId(String.valueOf(booking.getBookingId()));
                    vo.setBookingId(String.valueOf(booking.getBookingId()));
                    vo.setShareId(String.valueOf(booking.getShareId()));
                    vo.setUserId(String.valueOf(userId));
                    vo.setAmount(booking.getDeposit());
                    vo.setStatus(booking.getDepositReturned() == 1 ? 2 : 1); // 1-已支付, 2-已退还

                    // 设置时间字段（使用 DateTimeFormatter 格式化）
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    if (booking.getCreateTime() != null) {
                        vo.setCreateTime(booking.getCreateTime().format(formatter));
                    }
                    if (booking.getDepositReturnTime() != null) {
                        vo.setRefundTime(booking.getDepositReturnTime().format(formatter));
                    }

                    // 设置共享物品信息
                    if (shareItem != null) {
                        vo.setShareItemTitle(shareItem.getTitle());

                        // 查询封面图片
                        LambdaQueryWrapper<ShareItemImage> imageWrapper = new LambdaQueryWrapper<ShareItemImage>()
                                .eq(ShareItemImage::getShareId, shareItem.getShareId())
                                .eq(ShareItemImage::getIsCover, 1)
                                .eq(ShareItemImage::getStatus, 0);
                        ShareItemImage coverImage = shareItemImageMapper.selectOne(imageWrapper);
                        if (coverImage != null) {
                            vo.setShareItemCover(coverImage.getImageUrl());
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        recordPage.setRecords(records);

        return Result.success(recordPage);
    }
}
