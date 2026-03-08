package com.xx.xianqijava.service;

import com.xx.xianqijava.vo.UserLoginVO;

/**
 * 第三方登录服务接口
 */
public interface ThirdPartyLoginService {

    /**
     * 微信授权登录
     *
     * @param code 微信授权码
     * @return 登录用户信息
     */
    UserLoginVO loginByWechat(String code);

    /**
     * QQ授权登录
     *
     * @param code QQ授权码
     * @return 登录用户信息
     */
    UserLoginVO loginByQQ(String code);

    /**
     * 验证微信签名
     *
     * @param signature 微信签名
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param echostr 随机字符串
     * @return 验证结果
     */
    boolean verifyWechatSignature(String signature, String timestamp, String nonce, String echostr);
}
