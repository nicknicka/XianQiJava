package com.xx.xianqijava.interceptor;

import com.xx.xianqijava.service.UserActiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

/**
 * 用户活跃时间拦截器
 * 自动更新用户最后活跃时间
 */
@Slf4j
@Component
public class UserActiveInterceptor implements HandlerInterceptor {

    @Autowired
    private UserActiveService userActiveService;

    @Autowired
    private com.xx.xianqijava.util.JwtUtil jwtUtil;

    /**
     * 跳过的路径前缀
     */
    private static final String[] EXCLUDE_PATHS = {
        "/api/public",      // 公开接口
        "/api/doc",          // API文档
        "/swagger",         // Swagger
        "/webjars",         // Swagger资源
        "/static",          // 静态资源
        "/favicon.ico"       // 网站图标
    };

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

        String requestUri = request.getRequestURI();

        // 检查是否需要跳过
        if (shouldExclude(requestUri)) {
            return true;
        }

        // 只处理API请求
        if (!requestUri.startsWith("/api/")) {
            return true;
        }

        try {
            // 从请求中获取用户ID
            String userId = extractUserId(request);

            if (userId != null && !userId.isEmpty()) {
                // 异步更新活跃时间，不阻塞请求
                CompletableFuture.runAsync(() -> {
                    try {
                        userActiveService.updateLastActiveTime(userId);
                    } catch (Exception e) {
                        // 静默处理错误，避免影响主流程
                        log.debug("更新用户 {} 活跃时间失败（静默）", userId);
                    }
                });
            }
        } catch (Exception e) {
            // 静默处理错误，避免影响主流程
            log.debug("用户活跃时间拦截器处理异常: {}", e.getMessage());
        }

        return true;
    }

    /**
     * 判断是否需要跳过该路径
     */
    private boolean shouldExclude(String requestUri) {
        for (String exclude : EXCLUDE_PATHS) {
            if (requestUri.startsWith(exclude)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求中提取用户ID
     * 支持多种方式：Header、Token、Session
     */
    private String extractUserId(HttpServletRequest request) {
        // 方式1: 从 Header 获取（推荐）
        String userIdFromHeader = request.getHeader("X-User-Id");
        if (userIdFromHeader != null && !userIdFromHeader.isEmpty()) {
            return userIdFromHeader;
        }

        // 方式2: 从 Authorization Token 解析
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    return String.valueOf(userId);
                }
            } catch (Exception e) {
                log.debug("从JWT解析userId失败: {}", e.getMessage());
            }
        }

        // 方式3: 从 Session 获取
        // Object userIdObj = request.getAttribute("userId");
        // if (userIdObj != null) {
        //     return userIdObj.toString();
        // }

        return null;
    }
}
