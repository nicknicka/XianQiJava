package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、注册、验证码等认证相关功能
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final VerificationCodeService verificationCodeService;

    /**
     * 发送注册验证码
     */
    @PostMapping("/send-register-code")
    @Operation(summary = "发送注册验证码", description = "向指定手机号发送注册验证码")
    public Result<String> sendRegisterCode(
            @Parameter(description = "手机号码", required = true)
            @RequestParam String phoneNumber) {

        log.info("请求发送注册验证码：phone={}", phoneNumber);

        // 验证手机号格式
        if (!isValidPhoneNumber(phoneNumber)) {
            return Result.error("手机号格式不正确");
        }

        // 检查是否可以发送
        if (!verificationCodeService.canSendCode(phoneNumber, "register")) {
            int remainingAttempts = verificationCodeService.getRemainingAttempts(phoneNumber, "register");
            if (remainingAttempts <= 0) {
                return Result.error("今日发送次数已达上限，请明天再试");
            } else {
                return Result.error("发送过于频繁，请稍后再试");
            }
        }

        // 发送验证码
        String code = verificationCodeService.sendVerificationCode(phoneNumber, "register");
        if (code != null) {
            return Result.success("验证码发送成功，请注意查收");
        } else {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 发送登录验证码
     */
    @PostMapping("/send-login-code")
    @Operation(summary = "发送登录验证码", description = "向指定手机号发送登录验证码")
    public Result<String> sendLoginCode(
            @Parameter(description = "手机号码", required = true)
            @RequestParam String phoneNumber) {

        log.info("请求发送登录验证码：phone={}", phoneNumber);

        // 验证手机号格式
        if (!isValidPhoneNumber(phoneNumber)) {
            return Result.error("手机号格式不正确");
        }

        // 检查是否可以发送
        if (!verificationCodeService.canSendCode(phoneNumber, "login")) {
            int remainingAttempts = verificationCodeService.getRemainingAttempts(phoneNumber, "login");
            if (remainingAttempts <= 0) {
                return Result.error("今日发送次数已达上限，请明天再试");
            } else {
                return Result.error("发送过于频繁，请稍后再试");
            }
        }

        // 发送验证码
        String code = verificationCodeService.sendVerificationCode(phoneNumber, "login");
        if (code != null) {
            return Result.success("验证码发送成功，请注意查收");
        } else {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 发送重置密码验证码
     */
    @PostMapping("/send-reset-password-code")
    @Operation(summary = "发送重置密码验证码", description = "向指定手机号发送重置密码验证码")
    public Result<String> sendResetPasswordCode(
            @Parameter(description = "手机号码", required = true)
            @RequestParam String phoneNumber) {

        log.info("请求发送重置密码验证码：phone={}", phoneNumber);

        // 验证手机号格式
        if (!isValidPhoneNumber(phoneNumber)) {
            return Result.error("手机号格式不正确");
        }

        // 检查是否可以发送
        if (!verificationCodeService.canSendCode(phoneNumber, "reset_password")) {
            int remainingAttempts = verificationCodeService.getRemainingAttempts(phoneNumber, "reset_password");
            if (remainingAttempts <= 0) {
                return Result.error("今日发送次数已达上限，请明天再试");
            } else {
                return Result.error("发送过于频繁，请稍后再试");
            }
        }

        // 发送验证码
        String code = verificationCodeService.sendVerificationCode(phoneNumber, "reset_password");
        if (code != null) {
            return Result.success("验证码发送成功，请注意查收");
        } else {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    @Operation(summary = "验证验证码", description = "验证用户输入的验证码是否正确")
    public Result<Boolean> verifyCode(
            @Parameter(description = "手机号码", required = true)
            @RequestParam String phoneNumber,
            @Parameter(description = "验证码", required = true)
            @RequestParam String code,
            @Parameter(description = "验证码类型", required = true)
            @RequestParam String type) {

        log.info("验证验证码：phone={}, type={}", phoneNumber, type);

        boolean valid = verificationCodeService.verifyCode(phoneNumber, code, type);
        if (valid) {
            return Result.success("验证码验证成功", true);
        } else {
            return Result.error("验证码错误或已过期");
        }
    }

    /**
     * 检查是否可以发送验证码
     */
    @GetMapping("/can-send-code")
    @Operation(summary = "检查是否可发送验证码", description = "检查指定手机号是否可以发送验证码")
    public Result<CanSendCodeVO> canSendCode(
            @Parameter(description = "手机号码", required = true)
            @RequestParam String phoneNumber,
            @Parameter(description = "验证码类型", required = true)
            @RequestParam String type) {

        boolean canSend = verificationCodeService.canSendCode(phoneNumber, type);
        int remainingAttempts = verificationCodeService.getRemainingAttempts(phoneNumber, type);

        CanSendCodeVO vo = new CanSendCodeVO();
        vo.setCanSend(canSend);
        vo.setRemainingAttempts(remainingAttempts);
        vo.setReason(canSend ? "可以发送" : "发送过于频繁或次数已达上限");

        return Result.success(vo);
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        // 中国大陆手机号格式：1开头，第二位是3-9，总共11位数字
        return phoneNumber.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 是否可发送验证码的返回对象
     */
    public static class CanSendCodeVO {
        private Boolean canSend;
        private Integer remainingAttempts;
        private String reason;

        public Boolean getCanSend() {
            return canSend;
        }

        public void setCanSend(Boolean canSend) {
            this.canSend = canSend;
        }

        public Integer getRemainingAttempts() {
            return remainingAttempts;
        }

        public void setRemainingAttempts(Integer remainingAttempts) {
            this.remainingAttempts = remainingAttempts;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
