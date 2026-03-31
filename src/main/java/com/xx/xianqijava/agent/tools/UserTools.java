package com.xx.xianqijava.agent.tools;

import com.xx.xianqijava.agent.context.AIContext;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 用户相关工具集
 * 使用 LangChain4j 的 @Tool 注解声明工具函数
 *
 * @author Claude
 * @since 2026-03-23
 */
@Service
public class UserTools {

    private static final Logger log = LoggerFactory.getLogger(UserTools.class);

    @Resource
    private UserService userService;

    /**
     * 获取当前用户信息
     * 从 AIContext 中自动获取当前用户ID
     */
    @Tool("获取当前用户的基本信息，包括用户名、昵称、信用分等")
    public String getCurrentUserInfo() {
        Long userId = AIContext.getCurrentUserId();
        log.info("执行工具：getCurrentUserInfo，参数：{}", userId);

        if (userId == null) {
            return "无法获取当前用户ID";
        }

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return "用户不存在";
            }

            return String.format("""
                    用户信息：
                    - 用户ID：%d
                    - 用户名：%s
                    - 昵称：%s
                    - 信用分：%d
                    - 实名认证：%s
                    """,
                    user.getUserId(),
                    user.getUsername(),
                    user.getNickname() != null ? user.getNickname() : "未设置",
                    user.getCreditScore() != null ? user.getCreditScore() : 100,
                    user.getIsVerified() != null && user.getIsVerified() == 1 ? "已认证" : "未认证"
            );

        } catch (Exception e) {
            log.error("获取用户信息失败：userId={}", userId, e);
            return "获取用户信息失败：" + e.getMessage();
        }
    }

    /**
     * 检查当前用户信用等级
     * 从 AIContext 中自动获取当前用户ID
     */
    @Tool("检查当前用户的信用等级和信用分数")
    public String getCurrentUserCreditLevel() {
        Long userId = AIContext.getCurrentUserId();
        log.info("执行工具：getCurrentUserCreditLevel，参数：{}", userId);

        if (userId == null) {
            return "无法获取当前用户ID";
        }
        log.info("执行工具：getUserCreditLevel，参数：{}", userId);

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return "用户不存在";
            }

            Integer creditScore = user.getCreditScore() != null ? user.getCreditScore() : 100;

            String level;
            if (creditScore >= 150) {
                level = "AAA（优秀）";
            } else if (creditScore >= 120) {
                level = "AA（良好）";
            } else if (creditScore >= 100) {
                level = "A（正常）";
            } else if (creditScore >= 80) {
                level = "B（一般）";
            } else {
                level = "C（较差）";
            }

            return String.format("""
                    用户信用等级：%s
                    当前信用分：%d
                    说明：信用分越高，代表用户信誉越好，交易更可靠
                    """,
                    level, creditScore
            );

        } catch (Exception e) {
            log.error("获取用户信用等级失败：userId={}", userId, e);
            return "获取用户信用等级失败：" + e.getMessage();
        }
    }
}
