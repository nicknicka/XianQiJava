package com.xx.xianqijava.websocket;

import com.xx.xianqijava.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(org.springframework.http.ServerHttpRequest request,
                                   org.springframework.http.ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // 从查询参数中获取 Token
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.substring(query.indexOf("token=") + 6);
            if (token.contains("&")) {
                token = token.substring(0, token.indexOf("&"));
            }

            // 验证 Token
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                if (userId != null && username != null) {
                    // 将用户信息存储到 WebSocket 会话属性中
                    attributes.put("userId", userId);
                    attributes.put("username", username);
                    log.info("WebSocket 握手成功: userId={}, username={}", userId, username);
                    return true;
                }
            }
        }

        log.warn("WebSocket 握手失败：无效的 Token");
        return false;
    }

    @Override
    public void afterHandshake(org.springframework.http.ServerHttpRequest request,
                              org.springframework.http.ServerHttpResponse response,
                              WebSocketHandler wsHandler,
                              Exception exception) {
        if (exception != null) {
            log.error("WebSocket 握手后发生异常: {}", exception.getMessage(), exception);
        }
    }
}
