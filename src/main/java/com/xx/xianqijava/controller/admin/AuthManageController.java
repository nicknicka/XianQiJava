package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.annotation.OperationLog;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.UserRealNameAuth;
import com.xx.xianqijava.entity.UserStudentAuth;
import com.xx.xianqijava.service.UserRealNameAuthService;
import com.xx.xianqijava.service.UserStudentAuthService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.RealNameAuthVO;
import com.xx.xianqijava.vo.StudentAuthVO;
import com.xx.xianqijava.vo.admin.AuthManageStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证管理控制器 - 管理端
 * 用于管理用户实名认证和学生认证的审核
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "管理员审核用户认证接口")
public class AuthManageController {

    private final UserRealNameAuthService realNameAuthService;
    private final UserStudentAuthService studentAuthService;

    // ==================== 实名认证管理 ====================

    /**
     * 获取待审核的实名认证列表
     */
    @GetMapping("/real-name/pending")
    @Operation(summary = "获取待审核的实名认证列表")
    public Result<IPage<RealNameAuthVO>> getPendingRealNameAuthList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("管理员查询待审核的实名认证列表, page={}", current);

        Page<UserRealNameAuth> page = new Page<>(current, size);
        IPage<RealNameAuthVO> result = realNameAuthService.getPendingList(page);
        return Result.success(result);
    }

    /**
     * 获取所有实名认证列表
     */
    @GetMapping("/real-name/all")
    @Operation(summary = "获取所有实名认证列表")
    public Result<IPage<RealNameAuthVO>> getAllRealNameAuthList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        log.info("管理员查询所有实名认证列表, page={}, status={}", current, status);

        Page<UserRealNameAuth> page = new Page<>(current, size);
        IPage<RealNameAuthVO> result = realNameAuthService.getAllList(page, status);
        return Result.success(result);
    }

    /**
     * 获取实名认证详情
     */
    @GetMapping("/real-name/{authId}")
    @Operation(summary = "获取实名认证详情")
    public Result<RealNameAuthVO> getRealNameAuthDetail(
            @Parameter(description = "认证ID") @PathVariable Long authId) {
        log.info("管理员获取实名认证详情, authId={}", authId);

        RealNameAuthVO result = realNameAuthService.getAuthDetail(authId);
        return Result.success(result);
    }

    /**
     * 审核实名认证
     */
    @PutMapping("/real-name/audit")
    @Operation(summary = "审核实名认证")
    @OperationLog(
            module = "auth",
            action = "audit_real_name",
            description = "审核实名认证"
    )
    public Result<Void> auditRealNameAuth(@Valid @RequestBody AuthAuditDTO auditDTO) {
        Long auditorId = SecurityUtil.getCurrentUserIdRequired();

        // 将action转换为status
        int status = "approve".equals(auditDTO.getAction()) ? 2 : 3;

        log.info("管理员审核实名认证, authId={}, action={}, status={}, auditorId={}",
                auditDTO.getAuthId(), auditDTO.getAction(), status, auditorId);

        realNameAuthService.auditAuth(auditDTO.getAuthId(), auditorId,
                status, auditDTO.getRejectReason());
        return Result.success("审核完成");
    }

    // ==================== 学生认证管理 ====================

    /**
     * 获取待审核的学生认证列表
     */
    @GetMapping("/student/pending")
    @Operation(summary = "获取待审核的学生认证列表")
    public Result<IPage<StudentAuthVO>> getPendingStudentAuthList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("管理员查询待审核的学生认证列表, page={}", current);

        Page<UserStudentAuth> page = new Page<>(current, size);
        IPage<StudentAuthVO> result = studentAuthService.getPendingList(page);
        return Result.success(result);
    }

    /**
     * 获取所有学生认证列表
     */
    @GetMapping("/student/all")
    @Operation(summary = "获取所有学生认证列表")
    public Result<IPage<StudentAuthVO>> getAllStudentAuthList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        log.info("管理员查询所有学生认证列表, page={}, status={}", current, status);

        Page<UserStudentAuth> page = new Page<>(current, size);
        IPage<StudentAuthVO> result = studentAuthService.getAllList(page, status);
        return Result.success(result);
    }

    /**
     * 获取学生认证详情
     */
    @GetMapping("/student/{authId}")
    @Operation(summary = "获取学生认证详情")
    public Result<StudentAuthVO> getStudentAuthDetail(
            @Parameter(description = "认证ID") @PathVariable Long authId) {
        log.info("管理员获取学生认证详情, authId={}", authId);

        StudentAuthVO result = studentAuthService.getAuthDetail(authId);
        return Result.success(result);
    }

    /**
     * 审核学生认证
     */
    @PutMapping("/student/audit")
    @Operation(summary = "审核学生认证")
    @OperationLog(
            module = "auth",
            action = "audit_student",
            description = "审核学号认证"
    )
    public Result<Void> auditStudentAuth(@Valid @RequestBody AuthAuditDTO auditDTO) {
        Long auditorId = SecurityUtil.getCurrentUserIdRequired();

        // 将action转换为status
        int status = "approve".equals(auditDTO.getAction()) ? 2 : 3;

        log.info("管理员审核学生认证, authId={}, action={}, status={}, auditorId={}",
                auditDTO.getAuthId(), auditDTO.getAction(), status, auditorId);

        studentAuthService.auditAuth(auditDTO.getAuthId(), auditorId,
                status, auditDTO.getRejectReason());
        return Result.success("审核完成");
    }

    // ==================== 统计管理 ====================

    /**
     * 获取认证管理统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取认证管理统计信息")
    public Result<AuthManageStatistics> getAuthStatistics() {
        log.info("管理员获取认证统计信息");

        // 获取实名认证统计
        Map<String, Long> realNameStats = realNameAuthService.getStatistics();

        // 获取学号认证统计
        Map<String, Long> studentStats = studentAuthService.getStatistics();

        // 组装统计数据
        AuthManageStatistics statistics = new AuthManageStatistics();
        statistics.setRealNameTotal(realNameStats.get("total"));
        statistics.setRealNamePending(realNameStats.get("pending"));
        statistics.setRealNameApproved(realNameStats.get("approved"));
        statistics.setRealNameRejected(realNameStats.get("rejected"));
        statistics.setRealNameTodayApproved(realNameStats.get("todayApproved"));
        statistics.setRealNameTodayRejected(realNameStats.get("todayRejected"));

        statistics.setStudentTotal(studentStats.get("total"));
        statistics.setStudentPending(studentStats.get("pending"));
        statistics.setStudentApproved(studentStats.get("approved"));
        statistics.setStudentRejected(studentStats.get("rejected"));
        statistics.setStudentTodayApproved(studentStats.get("todayApproved"));
        statistics.setStudentTodayRejected(studentStats.get("todayRejected"));

        return Result.success(statistics);
    }

    // ==================== DTO ====================

    /**
     * 认证审核DTO
     */
    @Data
    public static class AuthAuditDTO {
        @NotNull(message = "认证ID不能为空")
        private Long authId;

        @NotNull(message = "审核操作不能为空")
        private String action; // approve-通过, reject-拒绝

        private String rejectReason;
    }
}
