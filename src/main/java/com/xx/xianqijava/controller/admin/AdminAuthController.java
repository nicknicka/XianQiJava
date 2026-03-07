package com.xx.xianqijava.controller.admin;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.admin.AdminLoginDTO;
import com.xx.xianqijava.security.SecurityContextHolder;
import com.xx.xianqijava.service.AdminService;
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

    /**
     * 管理员登录
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
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
    public Result<Void> logout() {
        log.info("管理员退出登录, adminId={}", SecurityContextHolder.getAdminId());

        // TODO: 可以将Token加入黑名单，实现真正的退出
        // 目前只是清除前端Token即可

        return Result.success("退出成功");
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
