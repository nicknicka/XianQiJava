package com.xx.xianqijava.security;

import com.xx.xianqijava.entity.Admin;
import com.xx.xianqijava.mapper.AdminMapper;
import com.xx.xianqijava.util.AdminJwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 管理员 JWT 认证过滤器
 * 专门用于验证管理员 API 的 Token
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminJwtUtil adminJwtUtil;
    private final AdminMapper adminMapper;

    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 只处理管理员路径
            String uri = request.getRequestURI();
            if (!uri.startsWith("/api/admin/")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从请求头中获取 Token
            String token = extractTokenFromRequest(request);

            // 验证 Token 并设置认证信息
            if (StringUtils.hasText(token) && adminJwtUtil.validateToken(token)) {
                Long adminId = adminJwtUtil.getAdminIdFromToken(token);
                String username = adminJwtUtil.getUsernameFromToken(token);

                if (adminId != null && username != null) {
                    // 查询管理员信息
                    Admin admin = adminMapper.selectById(adminId);

                    if (admin != null && admin.getIsActive() == 1) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        adminId,
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                );

                        // 设置详细信息
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 将认证信息设置到 Spring SecurityContext 中
                        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 设置到自定义上下文
                        SecurityContextHolder.setAdmin(admin);

                        log.debug("已设置管理员认证: adminId={}, username={}", adminId, username);
                    } else {
                        log.warn("管理员账号不存在或已被禁用: adminId={}", adminId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("无法设置管理员认证: {}", e.getMessage());
        } finally {
            // 清理自定义上下文
            if (!request.getRequestURI().startsWith("/api/admin/")) {
                SecurityContextHolder.clear();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取 Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
