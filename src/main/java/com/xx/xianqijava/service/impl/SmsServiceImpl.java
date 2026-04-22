package com.xx.xianqijava.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.xx.xianqijava.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信服务实现类
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.aliyun.region-id:cn-hangzhou}")
    private String regionId;

    @Value("${sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.aliyun.sign-name:}")
    private String signName;

    @Value("${sms.aliyun.template-register:}")
    private String registerTemplate;

    @Value("${sms.aliyun.template-login:}")
    private String loginTemplate;

    @Value("${sms.aliyun.template-reset:}")
    private String resetTemplate;

    @Value("${sms.aliyun.template-security-alert:}")
    private String securityAlertTemplate;

    @Value("${sms.aliyun.template-order-notify:}")
    private String orderNotifyTemplate;

    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        return sendTemplateSms(phoneNumber, resetTemplate, params, "重置验证码");
    }

    @Override
    public boolean sendLoginCode(String phoneNumber, String code) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        return sendTemplateSms(phoneNumber, loginTemplate, params, "登录验证码");
    }

    @Override
    public boolean sendRegisterCode(String phoneNumber, String code) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        return sendTemplateSms(phoneNumber, registerTemplate, params, "注册验证码");
    }

    @Override
    public boolean sendSecurityAlert(String phoneNumber, String message) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("message", message);
        return sendTemplateSms(phoneNumber, securityAlertTemplate, params, "安全提醒");
    }

    @Override
    public boolean sendOrderNotification(String phoneNumber, String orderNo, String status) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("orderNo", orderNo);
        params.put("status", status);
        return sendTemplateSms(phoneNumber, orderNotifyTemplate, params, "订单通知");
    }

    @Override
    public boolean sendSms(String phoneNumber, String templateId, String[] params) {
        Map<String, String> templateParams = new LinkedHashMap<>();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                templateParams.put("param" + (i + 1), params[i]);
            }
        }
        return sendTemplateSms(phoneNumber, templateId, templateParams, "自定义短信");
    }

    private boolean sendTemplateSms(String phoneNumber, String templateCode,
                                    Map<String, String> templateParams, String scene) {
        if (!smsEnabled) {
            log.warn("短信服务未启用，拒绝发送{}，phone={}", scene, phoneNumber);
            return false;
        }

        if (!isConfigured(templateCode)) {
            log.error("短信配置不完整，无法发送{}，phone={}", scene, phoneNumber);
            return false;
        }

        try {
            IAcsClient client = buildClient();
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain("dysmsapi.aliyuncs.com");
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");
            request.putQueryParameter("RegionId", regionId);
            request.putQueryParameter("PhoneNumbers", phoneNumber);
            request.putQueryParameter("SignName", signName);
            request.putQueryParameter("TemplateCode", templateCode);
            request.putQueryParameter("TemplateParam", JSONUtil.toJsonStr(templateParams));

            CommonResponse response = client.getCommonResponse(request);
            if (response.getHttpStatus() != 200) {
                log.error("短信发送失败，scene={}, phone={}, httpStatus={}, body={}",
                        scene, phoneNumber, response.getHttpStatus(), response.getData());
                return false;
            }

            Map<?, ?> responseMap = JSONUtil.parseObj(response.getData());
            String code = String.valueOf(responseMap.get("Code"));
            if (!"OK".equalsIgnoreCase(code)) {
                log.error("短信发送失败，scene={}, phone={}, providerCode={}, body={}",
                        scene, phoneNumber, code, response.getData());
                return false;
            }

            log.info("短信发送成功，scene={}, phone={}", scene, phoneNumber);
            return true;
        } catch (ClientException e) {
            log.error("短信发送异常，scene={}, phone={}", scene, phoneNumber, e);
            return false;
        }
    }

    private IAcsClient buildClient() throws ClientException {
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        return new DefaultAcsClient(profile);
    }

    private boolean isConfigured(String templateCode) {
        return StrUtil.isNotBlank(accessKeyId)
                && StrUtil.isNotBlank(accessKeySecret)
                && StrUtil.isNotBlank(signName)
                && StrUtil.isNotBlank(templateCode);
    }
}
