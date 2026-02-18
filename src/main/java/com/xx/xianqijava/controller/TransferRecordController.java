package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.TransferCreateDTO;
import com.xx.xianqijava.dto.TransferRespondDTO;
import com.xx.xianqijava.entity.TransferRecord;
import com.xx.xianqijava.service.TransferRecordService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.TransferRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 转赠记录控制器
 */
@Slf4j
@Tag(name = "物品转赠管理")
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferRecordController {

    private final TransferRecordService transferRecordService;

    /**
     * 发起转赠
     */
    @PostMapping
    @Operation(summary = "发起转赠")
    public Result<TransferRecordVO> createTransfer(@Valid @RequestBody TransferCreateDTO createDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("发起转赠, userId={}, shareId={}, toUserId={}",
                userId, createDTO.getShareId(), createDTO.getToUserId());
        TransferRecordVO recordVO = transferRecordService.createTransfer(createDTO, userId);
        return Result.success(recordVO);
    }

    /**
     * 响应转赠
     */
    @PutMapping("/respond")
    @Operation(summary = "响应转赠（接受或拒绝）")
    public Result<TransferRecordVO> respondTransfer(@Valid @RequestBody TransferRespondDTO respondDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("响应转赠, userId={}, transferId={}, status={}",
                userId, respondDTO.getTransferId(), respondDTO.getStatus());
        TransferRecordVO recordVO = transferRecordService.respondTransfer(respondDTO, userId);
        return Result.success(recordVO);
    }

    /**
     * 取消转赠
     */
    @DeleteMapping("/{transferId}")
    @Operation(summary = "取消转赠")
    public Result<Void> cancelTransfer(
            @Parameter(description = "转赠记录ID") @PathVariable Long transferId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("取消转赠, userId={}, transferId={}", userId, transferId);
        transferRecordService.cancelTransfer(transferId, userId);
        return Result.success();
    }

    /**
     * 获取转赠记录详情
     */
    @GetMapping("/{transferId}")
    @Operation(summary = "获取转赠记录详情")
    public Result<TransferRecordVO> getTransferRecord(
            @Parameter(description = "转赠记录ID") @PathVariable Long transferId) {
        log.info("获取转赠记录详情, transferId={}", transferId);
        TransferRecordVO recordVO = transferRecordService.getTransferRecord(transferId);
        return Result.success(recordVO);
    }

    /**
     * 获取我发起的转赠列表
     */
    @GetMapping("/sent")
    @Operation(summary = "获取我发起的转赠列表")
    public Result<IPage<TransferRecordVO>> getMySentTransfers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我发起的转赠, userId={}, page={}", userId, current);

        Page<TransferRecord> page = new Page<>(current, size);
        IPage<TransferRecordVO> result = transferRecordService.getMySentTransfers(page, userId);
        return Result.success(result);
    }

    /**
     * 获取我收到的转赠列表
     */
    @GetMapping("/received")
    @Operation(summary = "获取我收到的转赠列表")
    public Result<IPage<TransferRecordVO>> getMyReceivedTransfers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我收到的转赠, userId={}, page={}", userId, current);

        Page<TransferRecord> page = new Page<>(current, size);
        IPage<TransferRecordVO> result = transferRecordService.getMyReceivedTransfers(page, userId);
        return Result.success(result);
    }

    /**
     * 获取待确认的转赠列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待确认的转赠列表")
    public Result<List<TransferRecordVO>> getPendingTransfers() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询待确认的转赠, userId={}", userId);
        List<TransferRecordVO> result = transferRecordService.getPendingTransfers(userId);
        return Result.success(result);
    }
}
