package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.DepositPayDTO;
import com.xx.xianqijava.dto.DepositRefundDTO;
import com.xx.xianqijava.entity.DepositRecord;
import com.xx.xianqijava.service.DepositRecordService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.DepositRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 押金管理控制器
 */
@Slf4j
@Tag(name = "押金管理")
@RestController
@RequestMapping("/deposit")
@RequiredArgsConstructor
public class DepositRecordController {

    private final DepositRecordService depositRecordService;

    /**
     * 支付押金
     */
    @PostMapping("/pay")
    @Operation(summary = "支付押金")
    public Result<DepositRecordVO> payDeposit(@Valid @RequestBody DepositPayDTO payDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("支付押金, userId={}, bookingId={}", userId, payDTO.getBookingId());
        DepositRecordVO recordVO = depositRecordService.payDeposit(payDTO, userId);
        return Result.success("押金支付成功", recordVO);
    }

    /**
     * 退还押金
     */
    @PutMapping("/refund")
    @Operation(summary = "退还押金")
    public Result<DepositRecordVO> refundDeposit(@Valid @RequestBody DepositRefundDTO refundDTO) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("退还押金, ownerId={}, recordId={}", ownerId, refundDTO.getRecordId());
        DepositRecordVO recordVO = depositRecordService.refundDeposit(refundDTO, ownerId);
        return Result.success("押金退还成功", recordVO);
    }

    /**
     * 扣除押金
     */
    @PutMapping("/{recordId}/deduct")
    @Operation(summary = "扣除押金")
    public Result<Void> deductDeposit(
            @Parameter(description = "押金记录ID") @PathVariable("recordId") Long recordId,
            @Parameter(description = "扣除原因") @RequestParam String deductReason) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("扣除押金, recordId={}, ownerId={}, reason={}", recordId, ownerId, deductReason);
        depositRecordService.deductDeposit(recordId, deductReason, ownerId);
        return Result.success("押金已扣除");
    }

    /**
     * 获取押金记录详情
     */
    @GetMapping("/{recordId}")
    @Operation(summary = "获取押金记录详情")
    public Result<DepositRecordVO> getDepositRecord(
            @Parameter(description = "押金记录ID") @PathVariable("recordId") Long recordId) {
        log.info("查询押金记录详情, recordId={}", recordId);
        DepositRecordVO recordVO = depositRecordService.getDepositRecord(recordId);
        return Result.success(recordVO);
    }

    /**
     * 根据预约ID获取押金记录
     */
    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "根据预约ID获取押金记录")
    public Result<DepositRecordVO> getDepositByBookingId(
            @Parameter(description = "预约ID") @PathVariable("bookingId") Long bookingId) {
        log.info("根据预约ID查询押金记录, bookingId={}", bookingId);
        DepositRecordVO recordVO = depositRecordService.getDepositByBookingId(bookingId);
        return Result.success(recordVO);
    }

    /**
     * 获取我的押金记录列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的押金记录列表")
    public Result<IPage<DepositRecordVO>> getMyDepositRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的押金记录列表, userId={}, page={}", userId, page);

        Page<DepositRecord> pageParam = new Page<>(page, size);
        IPage<DepositRecordVO> recordPage = depositRecordService.getMyDepositRecords(pageParam, userId);

        return Result.success(recordPage);
    }
}
