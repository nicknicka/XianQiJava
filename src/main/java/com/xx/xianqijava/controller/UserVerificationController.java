package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.UserVerificationDTO;
import com.xx.xianqijava.dto.VerificationAuditDTO;
import com.xx.xianqijava.entity.UserVerification;
import com.xx.xianqijava.service.UserVerificationService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.UserVerificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户实名认证控制器
 */
@Slf4j
@Tag(name = "用户实名认证")
@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class UserVerificationController {

    private final UserVerificationService userVerificationService;

    /**
     * 提交实名认证
     */
    @PostMapping
    @Operation(summary = "提交实名认证")
    public Result<UserVerificationVO> submitVerification(@Valid @RequestBody UserVerificationDTO verificationDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("提交实名认证, userId={}, realName={}", userId, verificationDTO.getRealName());
        UserVerificationVO verificationVO = userVerificationService.submitVerification(verificationDTO, userId);
        return Result.success(verificationVO);
    }

    /**
     * 重新提交实名认证
     */
    @PostMapping("/resubmit")
    @Operation(summary = "重新提交实名认证")
    public Result<UserVerificationVO> resubmitVerification(@Valid @RequestBody UserVerificationDTO verificationDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("重新提交实名认证, userId={}, realName={}", userId, verificationDTO.getRealName());
        UserVerificationVO verificationVO = userVerificationService.resubmitVerification(verificationDTO, userId);
        return Result.success(verificationVO);
    }

    /**
     * 获取我的认证记录
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的认证记录")
    public Result<UserVerificationVO> getMyVerification() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的认证记录, userId={}", userId);
        UserVerificationVO verificationVO = userVerificationService.getMyVerification(userId);
        return Result.success(verificationVO);
    }

    /**
     * 获取认证记录详情
     */
    @GetMapping("/{verificationId}")
    @Operation(summary = "获取认证记录详情")
    public Result<UserVerificationVO> getVerificationDetail(
            @Parameter(description = "认证ID") @PathVariable Long verificationId) {
        log.info("获取认证记录详情, verificationId={}", verificationId);
        UserVerificationVO verificationVO = userVerificationService.getVerificationDetail(verificationId);
        return Result.success(verificationVO);
    }

    /**
     * 获取待审核的认证列表（管理员）
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待审核的认证列表（管理员）")
    public Result<IPage<UserVerificationVO>> getPendingVerifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询待审核的认证列表, page={}", current);

        Page<UserVerification> page = new Page<>(current, size);
        IPage<UserVerificationVO> result = userVerificationService.getPendingVerifications(page);
        return Result.success(result);
    }

    /**
     * 获取所有认证记录列表（管理员）
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有认证记录列表（管理员）")
    public Result<IPage<UserVerificationVO>> getAllVerifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        log.info("查询所有认证列表, page={}, status={}", current, status);

        Page<UserVerification> page = new Page<>(current, size);
        IPage<UserVerificationVO> result = userVerificationService.getAllVerifications(page, status);
        return Result.success(result);
    }

    /**
     * 审核实名认证（管理员）
     */
    @PutMapping("/audit")
    @Operation(summary = "审核实名认证（管理员）")
    public Result<UserVerificationVO> auditVerification(@Valid @RequestBody VerificationAuditDTO auditDTO) {
        Long auditorId = SecurityUtil.getCurrentUserIdRequired();
        log.info("审核实名认证, verificationId={}, status={}, auditorId={}",
                auditDTO.getVerificationId(), auditDTO.getStatus(), auditorId);
        UserVerificationVO verificationVO = userVerificationService.auditVerification(auditDTO, auditorId);
        return Result.success(verificationVO);
    }
}
