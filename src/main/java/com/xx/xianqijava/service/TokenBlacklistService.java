package com.xx.xianqijava.service;

/**
 * Token 黑名单服务接口
 */
public interface TokenBlacklistService {

    /**
     * 将 Token 加入黑名单
     *
     * @param token JWT Token
     * @param expirationSeconds Token 过期时间（秒）
     */
    void addToBlacklist(String token, long expirationSeconds);

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token JWT Token
     * @return true-在黑名单中，false-不在
     */
    boolean isBlacklisted(String token);

    /**
     * 清理过期的黑名单 Token
     */
    void cleanExpiredTokens();
}
