package com.xx.xianqijava.service;

/**
 * 短信服务接口
 *
 * @author Claude Code
 * @since 2026-03-08
 */
public interface SmsService {

    /**
     * 发送验证码短信
     *
     * @param phoneNumber 手机号码
     * @param code        验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phoneNumber, String code);

    /**
     * 发送登录验证码
     *
     * @param phoneNumber 手机号码
     * @param code        验证码
     * @return 是否发送成功
     */
    boolean sendLoginCode(String phoneNumber, String code);

    /**
     * 发送注册验证码
     *
     * @param phoneNumber 手机号码
     * @param code        验证码
     * @return 是否发送成功
     */
    boolean sendRegisterCode(String phoneNumber, String code);

    /**
     * 发送安全提醒短信
     *
     * @param phoneNumber 手机号码
     * @param message     提醒内容
     * @return 是否发送成功
     */
    boolean sendSecurityAlert(String phoneNumber, String message);

    /**
     * 发送订单通知短信
     *
     * @param phoneNumber 手机号码
     * @param orderNo     订单号
     * @param status      订单状态
     * @return 是否发送成功
     */
    boolean sendOrderNotification(String phoneNumber, String orderNo, String status);

    /**
     * 发送通用短信
     *
     * @param phoneNumber 手机号码
     * @param templateId  模板ID
     * @param params      模板参数
     * @return 是否发送成功
     */
    boolean sendSms(String phoneNumber, String templateId, String[] params);
}
