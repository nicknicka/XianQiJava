package com.xx.xianqijava.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Admin;
import com.xx.xianqijava.mapper.AdminMapper;
import com.xx.xianqijava.util.AdminJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员认证拦截器
 * 拦截所有/admin/*路径的请求，验证JWT Token
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AdminJwtUtil adminJwtUtil;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {
        // 1. 检查是否是管理员接口
        String uri = request.getRequestURI();
        if (!uri.startsWith("/admin/")) {
            return true;  // 非管理员接口，放行
        }

        // 2. 放行登录接口和静态资源
        if (uri.equals("/admin/auth/login") ||
            uri.startsWith("/admin/doc.html") ||
            uri.startsWith("/admin/swagger") ||
            uri.startsWith("/admin/webjars") ||
            uri.startsWith("/admin/v3/api-docs")) {
            return true;
        }

        // 3. 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }

        token = token.substring(7);  // 去掉 "Bearer " 前缀

        // 4. 验证Token
        try {
            if (!adminJwtUtil.validateToken(token)) {
                sendErrorResponse(response, 401, "Token无效或已过期，请重新登录");
                return false;
            }

            // 5. 获取管理员信息
            Long adminId = adminJwtUtil.getAdminIdFromToken(token);
            Admin admin = adminMapper.selectById(adminId);

            if (admin == null) {
                sendErrorResponse(response, 401, "管理员账号不存在");
                return false;
            }

            if (admin.getIsActive() == 0) {
                sendErrorResponse(response, 403, "管理员账号已被禁用");
                return false;
            }

            // 6. 设置到上下文
            SecurityContextHolder.setAdmin(admin);

            // 7. 记录请求日志
            log.debug("管理员访问: adminId={}, username={}, uri={}",
                    admin.getId(), admin.getUsername(), uri);

            return true;

        } catch (Exception e) {
            log.error("Token验证失败", e);
            sendErrorResponse(response, 401, "Token验证失败");
            return false;
        }
    }

    /**
     * 请求完成后清理上下文
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        SecurityContextHolder.clear();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response,
                                   int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");

        Result<Void> result = Result.error(message);

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}
