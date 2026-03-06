package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.RefundCreateDTO;
import com.xx.xianqijava.dto.RefundLogisticsDTO;
import com.xx.xianqijava.entity.RefundRecord;
import com.xx.xianqijava.service.RefundRecordService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.RefundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 退款记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
@Tag(name = "退款管理", description = "退款相关接口")
public class RefundController {

    private final RefundRecordService refundRecordService;

    /**
     * 创建退款申请
     */
    @PostMapping
    @Operation(summary = "创建退款申请")
    public Result<RefundVO> createRefund(@Valid @RequestBody RefundCreateDTO createDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建退款申请, userId={}, orderId={}", userId, createDTO.getOrderId());
        RefundVO refundVO = refundRecordService.createRefund(createDTO, userId);
        return Result.success("退款申请提交成功", refundVO);
    }

    /**
     * 获取退款详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取退款详情")
    public Result<RefundVO> getRefundDetail(
            @Parameter(description = "退款记录ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        RefundVO refundVO = refundRecordService.getRefundDetail(id, userId);
        return Result.success(refundVO);
    }

    /**
     * 分页查询买家的退款列表
     */
    @GetMapping("/buyer")
    @Operation(summary = "分页查询买家的退款列表")
    public Result<IPage<RefundVO>> getBuyerRefundList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态：0-待审核 1-已同意 2-已拒绝 3-退货中 4-已完成 5-已取消") @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Page<RefundRecord> pageParam = new Page<>(page, size);
        IPage<RefundVO> refundVOPage = refundRecordService.getBuyerRefundList(pageParam, userId, status);
        return Result.success(refundVOPage);
    }

    /**
     * 分页查询卖家的退款列表
     */
    @GetMapping("/seller")
    @Operation(summary = "分页查询卖家的退款列表")
    public Result<IPage<RefundVO>> getSellerRefundList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态：0-待审核 1-已同意 2-已拒绝 3-退货中 4-已完成 5-已取消") @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Page<RefundRecord> pageParam = new Page<>(page, size);
        IPage<RefundVO> refundVOPage = refundRecordService.getSellerRefundList(pageParam, userId, status);
        return Result.success(refundVOPage);
    }

    /**
     * 取消退款申请
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消退款申请")
    public Result<Void> cancelRefund(
            @Parameter(description = "退款记录ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("取消退款申请, refundId={}, userId={}", id, userId);
        refundRecordService.cancelRefund(id, userId);
        return Result.success("退款申请已取消");
    }

    /**
     * 同意退款申请（卖家）
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "同意退款申请")
    public Result<Void> approveRefund(
            @Parameter(description = "退款记录ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("同意退款申请, refundId={}, userId={}", id, userId);
        refundRecordService.approveRefund(id, userId);
        return Result.success("已同意退款申请");
    }

    /**
     * 拒绝退款申请（卖家）
     */
    @PutMapping("/{id}/reject")
    @Operation(summary = "拒绝退款申请")
    public Result<Void> rejectRefund(
            @Parameter(description = "退款记录ID") @PathVariable("id") Long id,
            @Parameter(description = "拒绝原因") @RequestParam(required = false) String rejectReason) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("拒绝退款申请, refundId={}, userId={}, reason={}", id, userId, rejectReason);
        refundRecordService.rejectRefund(id, userId, rejectReason);
        return Result.success("已拒绝退款申请");
    }

    /**
     * 填写退货物流（买家）
     */
    @PutMapping("/{id}/logistics")
    @Operation(summary = "填写退货物流")
    public Result<Void> fillLogistics(
            @Parameter(description = "退款记录ID") @PathVariable("id") Long id,
            @Valid @RequestBody RefundLogisticsDTO logisticsDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("填写退货物流, refundId={}, userId={}, company={}, no={}",
                id, userId, logisticsDTO.getLogisticsCompany(), logisticsDTO.getLogisticsNo());
        refundRecordService.fillLogistics(id, userId, logisticsDTO);
        return Result.success("物流信息已填写");
    }

    /**
     * 确认收货并完成退款（卖家）
     */
    @PutMapping("/{id}/confirm-return")
    @Operation(summary = "确认收货并完成退款")
    public Result<Void> confirmReturn(
            @Parameter(description = "退款记录ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("确认收货并完成退款, refundId={}, userId={}", id, userId);
        refundRecordService.confirmReturn(id, userId);
        return Result.success("退款已完成");
    }
}
