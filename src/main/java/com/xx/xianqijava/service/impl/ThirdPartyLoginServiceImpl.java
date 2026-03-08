package com.xx.xianqijava.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ThirdPartyLoginService;
import com.xx.xianqijava.util.JwtUtil;
import com.xx.xianqijava.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方登录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyLoginServiceImpl implements ThirdPartyLoginService {

    private final UserMapper userMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtUtil jwtUtil;

    @Value("${third-party.wechat.app-id:}")
    private String wechatAppId;

    @Value("${third-party.wechat.app-secret:}")
    private String wechatAppSecret;

    @Value("${third-party.wechat.enabled:false}")
    private Boolean wechatEnabled;

    @Value("${third-party.qq.app-id:}")
    private String qqAppId;

    @Value("${third-party.qq.app-key:}")
    private String qqAppKey;

    @Value("${third-party.qq.enabled:false}")
    private Boolean qqEnabled;

    @Override
    public UserLoginVO loginByWechat(String code) {
        log.info("微信授权登录请求, code={}", code);

        // 检查是否启用微信登录
        if (!wechatEnabled || wechatAppId == null || wechatAppId.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "微信登录功能暂未开放，请使用其他登录方式");
        }

        try {
            // 1. 通过 code 获取 access_token
            String tokenUrl = String.format(
                    "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                    wechatAppId, wechatAppSecret, code
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.getForObject(tokenUrl, Map.class);
            if (tokenResponse == null || tokenResponse.containsKey("errcode")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "微信授权失败，请重试");
            }

            String accessToken = (String) tokenResponse.get("access_token");
            String openid = (String) tokenResponse.get("openid");

            // 2. 通过 access_token 和 openid 获取用户信息
            String userInfoUrl = String.format(
                    "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
                    accessToken, openid
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = restTemplate.getForObject(userInfoUrl, Map.class);
            if (userInfo == null || userInfo.containsKey("errcode")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "获取微信用户信息失败");
            }

            String unionid = (String) userInfo.get("unionid");
            String nickname = (String) userInfo.get("nickname");
            String headImgUrl = (String) userInfo.get("headimgurl");

            // 3. 查找或创建用户
            User user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getWechatOpenid, openid)
            );

            if (user == null) {
                // 创建新用户
                user = new User();
                user.setWechatOpenid(openid);
                user.setWechatUnionid(unionid);
                user.setNickname(nickname);
                user.setAvatar(headImgUrl);
                user.setStatus(1); // 正常状态

                userMapper.insert(user);
                log.info("创建新用户(微信登录), userId={}, openid={}", user.getUserId(), openid);
            } else {
                // 更新用户信息
                user.setNickname(nickname);
                user.setAvatar(headImgUrl);
                if (unionid != null) {
                    user.setWechatUnionid(unionid);
                }
                userMapper.updateById(user);
                log.info("更新用户信息(微信登录), userId={}, openid={}", user.getUserId(), openid);
            }

            // 4. 生成 JWT token
            String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());

            // 5. 构建 VO
            UserLoginVO vo = new UserLoginVO();
            vo.setId(user.getUserId());
            vo.setToken(token);
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setPhone(user.getPhone());

            log.info("微信登录成功, userId={}, openid={}", user.getUserId(), openid);
            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录失败, code={}, error={}", code, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "微信登录失败，请稍后重试");
        }
    }

    @Override
    public UserLoginVO loginByQQ(String code) {
        log.info("QQ授权登录请求, code={}", code);

        // 检查是否启用QQ登录
        if (!qqEnabled || qqAppId == null || qqAppId.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "QQ登录功能暂未开放，请使用其他登录方式");
        }

        try {
            // 1. 通过 code 获取 access_token
            String tokenUrl = String.format(
                    "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri={redirect_uri}",
                    qqAppId, qqAppKey, code
            );

            String tokenResponse = restTemplate.getForObject(tokenUrl, String.class);
            if (tokenResponse == null || tokenResponse.contains("error")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "QQ授权失败，请重试");
            }

            // 解析响应获取 access_token
            Map<String, String> tokenMap = parseQueryString(tokenResponse);
            String accessToken = tokenMap.get("access_token");

            // 2. 获取 openid
            String openIdUrl = String.format(
                    "https://graph.qq.com/oauth2.0/me?access_token=%s",
                    accessToken
            );

            String openIdResponse = restTemplate.getForObject(openIdUrl, String.class);
            if (openIdResponse == null || openIdResponse.contains("error")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "获取QQ OpenID失败");
            }

            // 解析响应获取 openid (格式: client_id="xxx";openid="xxx")
            String openid = extractJsonValue(openIdResponse, "openid");

            // 3. 获取用户信息
            String userInfoUrl = String.format(
                    "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s",
                    accessToken, qqAppId, openid
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = restTemplate.getForObject(userInfoUrl, Map.class);
            if (userInfo == null || userInfo.containsKey("ret") && !userInfo.get("ret").equals(0)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "获取QQ用户信息失败");
            }

            String nickname = (String) userInfo.get("nickname");
            String headImgUrl = (String) userInfo.get("figureurl_qq_2"); // QQ头像100x100

            // 4. 查找或创建用户
            User user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getQqOpenid, openid)
            );

            if (user == null) {
                // 创建新用户
                user = new User();
                user.setQqOpenid(openid);
                user.setNickname(nickname);
                user.setAvatar(headImgUrl);
                user.setStatus(1); // 正常状态

                userMapper.insert(user);
                log.info("创建新用户(QQ登录), userId={}, openid={}", user.getUserId(), openid);
            } else {
                // 更新用户信息
                user.setNickname(nickname);
                user.setAvatar(headImgUrl);
                userMapper.updateById(user);
                log.info("更新用户信息(QQ登录), userId={}, openid={}", user.getUserId(), openid);
            }

            // 5. 生成 JWT token
            String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());

            // 6. 构建 VO
            UserLoginVO vo = new UserLoginVO();
            vo.setId(user.getUserId());
            vo.setToken(token);
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setPhone(user.getPhone());

            log.info("QQ登录成功, userId={}, openid={}", user.getUserId(), openid);
            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("QQ登录失败, code={}, error={}", code, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "QQ登录失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyWechatSignature(String signature, String timestamp, String nonce, String echostr) {
        // 微信服务器验证逻辑
        // 1. 将 token、timestamp、nonce 三个参数进行字典序排序
        // 2. 将三个参数字符串拼接成一个字符串进行 SHA1 加密
        // 3. 加密后的字符串与 signature 对比，标识该请求来源于微信

        // 这里需要从配置中获取 token
        String token = ""; // 应该从配置文件中读取

        String[] arr = new String[]{token, timestamp, nonce};
        java.util.Arrays.sort(arr);

        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
        }

        String encrypted = DigestUtil.sha1Hex(sb.toString());
        return encrypted.equals(signature);
    }

    /**
     * 解析查询字符串
     */
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    /**
     * 从QQ返回的JSON字符串中提取值
     * 格式: callback( {"client_id":"xxx","openid":"xxx"} );
     */
    private String extractJsonValue(String response, String key) {
        // 移除 callback( 和 );
        response = response.trim();
        if (response.startsWith("callback(")) {
            response = response.substring(9);
        }
        if (response.endsWith(")")) {
            response = response.substring(0, response.length() - 1);
        }

        // 解析JSON
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> map = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            return (String) map.get(key);
        } catch (Exception e) {
            log.error("解析QQ响应失败, response={}", response, e);
            return null;
        }
    }
}
