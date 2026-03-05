package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.RealNameAuthSubmitDTO;
import com.xx.xianqijava.dto.StudentAuthSubmitDTO;
import com.xx.xianqijava.service.UserRealNameAuthService;
import com.xx.xianqijava.service.UserStudentAuthService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.RealNameAuthVO;
import com.xx.xianqijava.vo.StudentAuthVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 */
@Slf4j
@Tag(name = "用户认证")
@RestController
@RequestMapping("/user/verification")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserRealNameAuthService realNameAuthService;
    private final UserStudentAuthService studentAuthService;

    // ==================== 实名认证相关接口 ====================

    /**
     * 提交实名认证
     */
    @Operation(summary = "提交实名认证")
    @PostMapping("/real-name")
    public Result<Long> submitRealNameAuth(@Valid @RequestBody RealNameAuthSubmitDTO submitDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("提交实名认证, userId={}", userId);
        Long authId = realNameAuthService.submitAuth(userId, submitDTO);
        return Result.success("提交成功，请等待审核", authId);
    }

    /**
     * 获取实名认证信息
     */
    @Operation(summary = "获取实名认证信息")
    @GetMapping("/real-name")
    public Result<RealNameAuthVO> getRealNameAuth() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取实名认证信息, userId={}", userId);
        RealNameAuthVO result = realNameAuthService.getAuthInfo(userId);
        return Result.success(result);
    }

    // ==================== 学生认证相关接口 ====================

    /**
     * 提交学生认证
     */
    @Operation(summary = "提交学生认证")
    @PostMapping("/student")
    public Result<Long> submitStudentAuth(@Valid @RequestBody StudentAuthSubmitDTO submitDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("提交学生认证, userId={}", userId);
        Long authId = studentAuthService.submitAuth(userId, submitDTO);
        return Result.success("提交成功，请等待审核", authId);
    }

    /**
     * 获取学生认证信息
     */
    @Operation(summary = "获取学生认证信息")
    @GetMapping("/student")
    public Result<StudentAuthVO> getStudentAuth() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取学生认证信息, userId={}", userId);
        StudentAuthVO result = studentAuthService.getAuthInfo(userId);
        return Result.success(result);
    }
}
