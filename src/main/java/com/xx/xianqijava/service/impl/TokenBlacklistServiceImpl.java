package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现类
 * 使用 Redis 存储黑名单 Token，自动过期
 */
@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String BLACKLIST_SET = "token:blacklist:all";

    public TokenBlacklistServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToBlacklist(String token, long expirationSeconds) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            // 将 Token 加入黑名单，设置过期时间
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", expirationSeconds, TimeUnit.SECONDS);

            // 同时加入集合，便于查询和管理
            redisTemplate.opsForSet().add(BLACKLIST_SET, token);

            log.debug("Token 已加入黑名单, expirationSeconds={}", expirationSeconds);
        } catch (Exception e) {
            log.error("将 Token 加入黑名单失败, error={}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("检查 Token 黑名单失败, error={}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void cleanExpiredTokens() {
        try {
            // 获取所有黑名单 Token
            Set<String> tokens = redisTemplate.opsForSet().members(BLACKLIST_SET);
            if (tokens == null || tokens.isEmpty()) {
                return;
            }

            // 检查每个 Token 是否还存在，不存在则从集合中移除
            int cleanedCount = 0;
            for (String token : tokens) {
                String key = BLACKLIST_PREFIX + token;
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                    redisTemplate.opsForSet().remove(BLACKLIST_SET, token);
                    cleanedCount++;
                }
            }

            if (cleanedCount > 0) {
                log.info("清理过期的黑名单 Token, count={}", cleanedCount);
            }
        } catch (Exception e) {
            log.error("清理过期黑名单 Token 失败, error={}", e.getMessage(), e);
        }
    }
}
