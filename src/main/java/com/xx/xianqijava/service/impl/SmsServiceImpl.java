package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 短信服务实现类（模拟实现）
 *
 * 生产环境需要对接真实的短信服务商（阿里云、腾讯云等）
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.mock-mode:true}")
    private boolean mockMode;

    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        if (!smsEnabled) {
            log.info("短信服务未启用，跳过发送验证码：phone={}, code={}", phoneNumber, code);
            return true; // 开发环境直接返回成功
        }

        if (mockMode) {
            log.info("【模拟短信】发送验证码 - 手机号：{}，验证码：{}", phoneNumber, code);
            return true;
        }

        // TODO: 对接真实的短信服务商
        // 这里是阿里云短信的示例代码
        // try {
        //     DefaultProfile profile = DefaultProfile.getProfile(
        //         "cn-hangzhou",
        //         accessKeyId,
        //         accessKeySecret
        //     );
        //     IAcsClient client = new DefaultAcsClient(profile);
        //
        //     SendSmsRequest request = new SendSmsRequest();
        //     request.setPhoneNumbers(phoneNumber);
        //     request.setSignName("签名");
        //     request.setTemplateCode("模板代码");
        //     request.setTemplateParam("{\"code\":\"" + code + "\"}");
        //
        //     SendSmsResponse response = client.getAcsResponse(request);
        //     return "OK".equals(response.getCode());
        // } catch (Exception e) {
        //     log.error("发送短信失败", e);
        //     return false;
        // }

        log.warn("短信真实发送模式未实现，请配置短信服务商");
        return false;
    }

    @Override
    public boolean sendLoginCode(String phoneNumber, String code) {
        log.info("发送登录验证码 - phone={}", phoneNumber);

        String message = String.format("【校园二手交易】您的登录验证码是：%s，5分钟内有效。如非本人操作，请忽略本短信。", code);
        return sendVerificationCode(phoneNumber, code);
    }

    @Override
    public boolean sendRegisterCode(String phoneNumber, String code) {
        log.info("发送注册验证码 - phone={}", phoneNumber);

        String message = String.format("【校园二手交易】您的注册验证码是：%s，10分钟内有效。欢迎加入校园二手交易平台！", code);
        return sendVerificationCode(phoneNumber, code);
    }

    @Override
    public boolean sendSecurityAlert(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("短信服务未启用，跳过发送安全提醒：phone={}, message={}", phoneNumber, message);
            return true;
        }

        if (mockMode) {
            log.info("【模拟短信】发送安全提醒 - 手机号：{}，内容：{}", phoneNumber, message);
            return true;
        }

        // TODO: 对接真实的短信服务商
        log.warn("短信真实发送模式未实现");
        return false;
    }

    @Override
    public boolean sendOrderNotification(String phoneNumber, String orderNo, String status) {
        log.info("发送订单通知 - phone={}, orderNo={}, status={}", phoneNumber, orderNo, status);

        String message = String.format("【校园二手交易】您的订单 %s 状态已变更为：%s。请及时查看订单详情。", orderNo, status);
        return sendSecurityAlert(phoneNumber, message);
    }

    @Override
    public boolean sendSms(String phoneNumber, String templateId, String[] params) {
        if (!smsEnabled) {
            log.info("短信服务未启用，跳过发送短信：phone={}, templateId={}", phoneNumber, templateId);
            return true;
        }

        if (mockMode) {
            log.info("【模拟短信】发送自定义短信 - 手机号：{}，模板ID：{}，参数：{}",
                phoneNumber, templateId, String.join(", ", params));
            return true;
        }

        // TODO: 对接真实的短信服务商
        log.warn("短信真实发送模式未实现");
        return false;
    }
}
