package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.BookingApproveDTO;
import com.xx.xianqijava.dto.BookingReturnDTO;
import com.xx.xianqijava.dto.ShareItemBookingCreateDTO;
import com.xx.xianqijava.entity.ShareItemBooking;
import com.xx.xianqijava.service.ShareItemBookingService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ShareItemBookingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 共享物品预约借用控制器
 */
@Slf4j
@Tag(name = "共享物品预约借用管理")
@RestController
@RequestMapping("/share-item-booking")
@RequiredArgsConstructor
public class ShareItemBookingController {

    private final ShareItemBookingService shareItemBookingService;

    /**
     * 创建预约借用
     */
    @PostMapping
    @Operation(summary = "创建预约借用")
    public Result<ShareItemBookingVO> createBooking(@Valid @RequestBody ShareItemBookingCreateDTO createDTO) {
        Long borrowerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建预约借用, borrowerId={}, shareId={}", borrowerId, createDTO.getShareId());
        ShareItemBookingVO bookingVO = shareItemBookingService.createBooking(createDTO, borrowerId);
        return Result.success("预约成功，等待所有者审批", bookingVO);
    }

    /**
     * 审批预约
     */
    @PutMapping("/approve")
    @Operation(summary = "审批预约借用")
    public Result<ShareItemBookingVO> approveBooking(@Valid @RequestBody BookingApproveDTO approveDTO) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("审批预约, bookingId={}, status={}, ownerId={}",
                approveDTO.getBookingId(), approveDTO.getStatus(), ownerId);
        ShareItemBookingVO bookingVO = shareItemBookingService.approveBooking(approveDTO, ownerId);
        return Result.success("审批成功", bookingVO);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{bookingId}/cancel")
    @Operation(summary = "取消预约借用")
    public Result<Void> cancelBooking(
            @Parameter(description = "预约ID") @PathVariable("bookingId") Long bookingId) {
        Long borrowerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("取消预约, bookingId={}, borrowerId={}", bookingId, borrowerId);
        shareItemBookingService.cancelBooking(bookingId, borrowerId);
        return Result.success("预约已取消");
    }

    /**
     * 确认归还
     */
    @PutMapping("/return")
    @Operation(summary = "确认归还物品")
    public Result<ShareItemBookingVO> confirmReturn(@Valid @RequestBody BookingReturnDTO returnDTO) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("确认归还, bookingId={}, ownerId={}", returnDTO.getBookingId(), ownerId);
        ShareItemBookingVO bookingVO = shareItemBookingService.confirmReturn(returnDTO, ownerId);
        return Result.success("归还确认成功", bookingVO);
    }

    /**
     * 退还押金
     */
    @PutMapping("/{bookingId}/deposit-return")
    @Operation(summary = "退还押金")
    public Result<Void> returnDeposit(
            @Parameter(description = "预约ID") @PathVariable("bookingId") Long bookingId) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("退还押金, bookingId={}, ownerId={}", bookingId, ownerId);
        shareItemBookingService.returnDeposit(bookingId, ownerId);
        return Result.success("押金已退还");
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{bookingId}")
    @Operation(summary = "获取预约详情")
    public Result<ShareItemBookingVO> getBookingDetail(
            @Parameter(description = "预约ID") @PathVariable("bookingId") Long bookingId) {
        log.info("查询预约详情, bookingId={}", bookingId);
        ShareItemBookingVO bookingVO = shareItemBookingService.getBookingDetail(bookingId);
        return Result.success(bookingVO);
    }

    /**
     * 获取我的预约列表（作为借用者）
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的预约列表")
    public Result<IPage<ShareItemBookingVO>> getMyBookings(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long borrowerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的预约列表, borrowerId={}, page={}", borrowerId, page);

        Page<ShareItemBooking> pageParam = new Page<>(page, size);
        IPage<ShareItemBookingVO> bookingPage = shareItemBookingService.getMyBookings(pageParam, borrowerId);

        return Result.success(bookingPage);
    }

    /**
     * 获取我收到的预约列表（作为所有者）
     */
    @GetMapping("/received")
    @Operation(summary = "获取收到的预约列表")
    public Result<IPage<ShareItemBookingVO>> getReceivedBookings(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询收到的预约列表, ownerId={}, page={}", ownerId, page);

        Page<ShareItemBooking> pageParam = new Page<>(page, size);
        IPage<ShareItemBookingVO> bookingPage = shareItemBookingService.getReceivedBookings(pageParam, ownerId);

        return Result.success(bookingPage);
    }
}
