package com.xx.xianqijava.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员JWT工具类
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Component
public class AdminJwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.admin-expiration:86400}")  // 默认24小时
    private Long expiration;

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成管理员Token
     */
    public String generateToken(Long adminId, String username, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("nickname", nickname);
        claims.put("userType", "admin");  // 标识为管理员
        claims.put("loginTime", System.currentTimeMillis());

        return createToken(claims);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(claims.get("adminId")))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取管理员ID
     */
    public Long getAdminIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("adminId", Long.class) : null;
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 验证管理员Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }

            // 检查是否是管理员Token
            String userType = claims.get("userType", String.class);
            if (!"admin".equals(userType)) {
                return false;
            }

            // 检查是否过期
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析Token
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 获取Token剩余有效时间（秒）
     */
    public Long getRemainingValidity(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return 0L;
        }
        long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return remaining > 0 ? remaining : 0L;
    }
}
