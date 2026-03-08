package com.xx.xianqijava.service;

/**
 * 验证码服务接口
 *
 * @author Claude Code
 * @since 2026-03-08
 */
public interface VerificationCodeService {

    /**
     * 生成并发送验证码
     *
     * @param phoneNumber 手机号码
     * @param type        验证码类型（register-注册，login-登录，reset_password-重置密码）
     * @return 验证码，如果发送失败返回null
     */
    String sendVerificationCode(String phoneNumber, String type);

    /**
     * 验证验证码
     *
     * @param phoneNumber 手机号码
     * @param code        验证码
     * @param type        验证码类型
     * @return 是否验证成功
     */
    boolean verifyCode(String phoneNumber, String code, String type);

    /**
     * 检查验证码是否存在
     *
     * @param phoneNumber 手机号码
     * @param type        验证码类型
     * @return 是否存在未过期的验证码
     */
    boolean hasValidCode(String phoneNumber, String type);

    /**
     * 删除验证码（验证成功后调用）
     *
     * @param phoneNumber 手机号码
     * @param type        验证码类型
     */
    void deleteCode(String phoneNumber, String type);

    /**
     * 获取剩余发送次数（防止频繁发送）
     *
     * @param phoneNumber 手机号码
     * @param type        验证码类型
     * @return 剩余次数
     */
    int getRemainingAttempts(String phoneNumber, String type);

    /**
     * 检查是否可以发送验证码
     *
     * @param phoneNumber 手机号码
     * @param type        验证码类型
     * @return 是否可以发送
     */
    boolean canSendCode(String phoneNumber, String type);
}
