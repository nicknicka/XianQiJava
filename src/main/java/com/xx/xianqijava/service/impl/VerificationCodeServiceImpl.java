package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.service.SmsService;
import com.xx.xianqijava.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final SmsService smsService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 验证码过期时间（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 发送间隔限制（秒）
     */
    private static final int SEND_INTERVAL_SECONDS = 60;

    /**
     * 每日最大发送次数
     */
    private static final int MAX_DAILY_SEND_COUNT = 10;

    /**
     * Redis key 前缀
     */
    private static final String CODE_KEY_PREFIX = "verify_code:";
    private static final String COUNT_KEY_PREFIX = "verify_count:";
    private static final String LAST_SEND_KEY_PREFIX = "last_send:";

    @Override
    public String sendVerificationCode(String phoneNumber, String type) {
        // 检查是否可以发送
        if (!canSendCode(phoneNumber, type)) {
            log.warn("发送验证码过于频繁：phone={}, type={}", phoneNumber, type);
            return null;
        }

        // 生成6位数字验证码
        String code = generateCode();

        // 存储验证码到Redis
        String codeKey = CODE_KEY_PREFIX + type + ":" + phoneNumber;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 记录发送次数
        String countKey = COUNT_KEY_PREFIX + type + ":" + phoneNumber;
        Long count = redisTemplate.opsForValue().increment(countKey);
        if (count == 1) {
            // 第一次发送，设置24小时过期
            redisTemplate.expire(countKey, 24, TimeUnit.HOURS);
        }

        // 记录最后发送时间
        String lastSendKey = LAST_SEND_KEY_PREFIX + type + ":" + phoneNumber;
        redisTemplate.opsForValue().set(lastSendKey, String.valueOf(System.currentTimeMillis()),
            SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 发送短信
        boolean sent = false;
        if ("register".equals(type)) {
            sent = smsService.sendRegisterCode(phoneNumber, code);
        } else if ("login".equals(type)) {
            sent = smsService.sendLoginCode(phoneNumber, code);
        } else if ("reset_password".equals(type)) {
            sent = smsService.sendVerificationCode(phoneNumber, code);
        } else {
            sent = smsService.sendVerificationCode(phoneNumber, code);
        }

        if (sent) {
            log.info("验证码发送成功 - phone={}, type={}, code={}", phoneNumber, type, code);
            return code;
        } else {
            // 发送失败，删除已存储的验证码
            redisTemplate.delete(codeKey);
            log.error("验证码发送失败 - phone={}, type={}", phoneNumber, type);
            return null;
        }
    }

    @Override
    public boolean verifyCode(String phoneNumber, String code, String type) {
        String codeKey = CODE_KEY_PREFIX + type + ":" + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            log.warn("验证码不存在或已过期 - phone={}, type={}", phoneNumber, type);
            return false;
        }

        boolean valid = storedCode.equals(code);
        if (valid) {
            log.info("验证码验证成功 - phone={}, type={}", phoneNumber, type);
            // 验证成功后删除验证码（一次性使用）
            deleteCode(phoneNumber, type);
        } else {
            log.warn("验证码验证失败 - phone={}, type={}, expected={}, actual={}",
                phoneNumber, type, storedCode, code);
        }

        return valid;
    }

    @Override
    public boolean hasValidCode(String phoneNumber, String type) {
        String codeKey = CODE_KEY_PREFIX + type + ":" + phoneNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(codeKey));
    }

    @Override
    public void deleteCode(String phoneNumber, String type) {
        String codeKey = CODE_KEY_PREFIX + type + ":" + phoneNumber;
        redisTemplate.delete(codeKey);
        log.debug("验证码已删除 - phone={}, type={}", phoneNumber, type);
    }

    @Override
    public int getRemainingAttempts(String phoneNumber, String type) {
        String countKey = COUNT_KEY_PREFIX + type + ":" + phoneNumber;
        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        return Math.max(0, MAX_DAILY_SEND_COUNT - currentCount);
    }

    @Override
    public boolean canSendCode(String phoneNumber, String type) {
        // 检查发送间隔
        String lastSendKey = LAST_SEND_KEY_PREFIX + type + ":" + phoneNumber;
        Boolean hasRecentSend = redisTemplate.hasKey(lastSendKey);
        if (Boolean.TRUE.equals(hasRecentSend)) {
            return false;
        }

        // 检查每日发送次数
        String countKey = COUNT_KEY_PREFIX + type + ":" + phoneNumber;
        String countStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        if (currentCount >= MAX_DAILY_SEND_COUNT) {
            log.warn("超过每日最大发送次数 - phone={}, type={}, count={}", phoneNumber, type, currentCount);
            return false;
        }

        return true;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
