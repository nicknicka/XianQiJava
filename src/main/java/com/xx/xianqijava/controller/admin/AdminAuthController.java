package com.xx.xianqijava.controller.admin;

import com.xx.xianqijava.annotation.OperationLog;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.admin.AdminLoginDTO;
import com.xx.xianqijava.security.SecurityContextHolder;
import com.xx.xianqijava.service.AdminService;
import com.xx.xianqijava.service.TokenBlacklistService;
import com.xx.xianqijava.vo.admin.AdminInfoVO;
import com.xx.xianqijava.vo.admin.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Slf4j
@Tag(name = "管理员认证", description = "管理员登录、获取信息等接口")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 管理员登录
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    @com.xx.xianqijava.annotation.OperationLog(
            module = "admin",
            action = "login",
            description = "管理员登录"
    )
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO dto,
                                     HttpServletRequest request) {
        log.info("管理员登录请求, username={}", dto.getUsername());

        AdminLoginVO result = adminService.login(dto);

        // 更新最后登录信息
        String ip = getClientIP(request);
        adminService.updateLastLoginInfo(result.getAdminInfo().getId(), ip);

        return Result.success("登录成功", result);
    }

    /**
     * 获取当前管理员信息
     */
    @Operation(summary = "获取当前管理员信息")
    @GetMapping("/info")
    public Result<AdminInfoVO> getAdminInfo() {
        Long adminId = SecurityContextHolder.getAdminId();
        log.info("获取管理员信息, adminId={}", adminId);

        AdminInfoVO result = adminService.getAdminInfo(adminId);
        return Result.success(result);
    }

    /**
     * 管理员退出登录
     */
    @Operation(summary = "管理员退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long adminId = SecurityContextHolder.getAdminId();
        log.info("管理员退出登录, adminId={}", adminId);

        // 从请求头中获取 Token
        String token = extractTokenFromRequest(request);
        if (token != null && !token.isEmpty()) {
            // 将 Token 加入黑名单，设置过期时间为 24 小时
            tokenBlacklistService.addToBlacklist(token, 86400);
            log.info("Token 已加入黑名单, adminId={}", adminId);
        }

        return Result.success("退出成功");
    }

    /**
     * 从请求中提取 Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
