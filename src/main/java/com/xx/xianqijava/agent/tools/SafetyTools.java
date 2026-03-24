package com.xx.xianqijava.agent.tools;

import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 安全相关工具集
 *
 * @author Claude
 * @since 2026-03-23
 */
@Service
public class SafetyTools {

    private static final Logger log = LoggerFactory.getLogger(SafetyTools.class);

    @Resource
    private UserService userService;

    /**
     * 检查用户信用
     *
     * @param userId 用户ID
     * @return 信用检查结果
     */
    @Tool("检查用户信用分数和信用等级")
    public String checkUserCredit(@P("用户ID") Long userId) {
        log.info("执行工具：checkUserCredit，userId={}", userId);

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return "用户不存在";
            }

            Integer creditScore = user.getCreditScore() != null ? user.getCreditScore() : 100;

            // 评估信用风险
            String riskLevel;
            String advice;

            if (creditScore >= 120) {
                riskLevel = "低风险";
                advice = "该用户信用良好，可以放心交易";
            } else if (creditScore >= 90) {
                riskLevel = "中等风险";
                advice = "该用户信用一般，建议优先选择校内面交";
            } else {
                riskLevel = "高风险";
                advice = "该用户信用较差，建议谨慎交易或使用平台担保";
            }

            return String.format("""
                    用户信用检查结果：
                    - 信用分数：%d
                    - 风险等级：%s
                    - 交易建议：%s
                    """,
                    creditScore,
                    riskLevel,
                    advice
            );

        } catch (Exception e) {
            log.error("检查用户信用失败：userId={}", userId, e);
            return "检查用户信用失败：" + e.getMessage();
        }
    }

    /**
     * 获取安全交易提示
     *
     * @return 安全提示
     */
    @Tool("获取安全交易提示和防骗指南")
    public String getSafetyTips() {
        log.info("执行工具：getSafetyTips");

        return """
                闲七平台安全交易指南

                【交易前】
                1. 核实对方身份
                   - 查看对方实名认证状态
                   - 查看对方信用分数
                   - 查看历史评价记录

                2. 选择安全交易方式
                   - 优先选择校内面交
                   - 使用平台担保交易
                   - 不要直接转账

                3. 了解商品情况
                   - 要求查看实物照片
                   - 了解商品使用情况
                   - 确认商品配件齐全

                【交易中】
                1. 面交注意事项
                   - 选择白天在人多的地方
                   - 最好有同学陪同
                   - 当面检查商品质量
                   - 确认无误后再付款

                2. 快递注意事项
                   - 使用平台担保交易
                   - 保留快递单号
                   - 收到货后检查再确认
                   - 拍照留证

                【交易后】
                1. 及时确认收货
                2. 客观评价交易
                3. 保留交易凭证
                4. 遇到问题及时举报

                【防骗要点】
                ✗ 不要提前付款
                ✗ 不要脱离平台交易
                ✗ 不要点击陌生链接
                ✗ 不要泄露个人信息
                ✓ 优先选择校内面交
                ✓ 使用平台担保交易
                ✓ 保留聊天和交易记录
                ✓ 发现问题及时举报
                """;
    }

    /**
     * 评估交易安全风险
     *
     * @param buyerId 买家ID
     * @param sellerId 卖家ID
     * @param price   交易金额
     * @return 风险评估结果
     */
    @Tool("评估交易安全风险")
    public String assessTradeRisk(
            @P("买家ID") Long buyerId,
            @P("卖家ID") Long sellerId,
            @P("交易金额") Double price
    ) {
        log.info("执行工具：assessTradeRisk，buyerId={}, sellerId={}, price={}",
                buyerId, sellerId, price);

        try {
            User buyer = userService.getById(buyerId);
            User seller = userService.getById(sellerId);

            if (buyer == null || seller == null) {
                return "用户信息不完整，无法评估风险";
            }

            Integer buyerCredit = buyer.getCreditScore() != null ? buyer.getCreditScore() : 100;
            Integer sellerCredit = seller.getCreditScore() != null ? seller.getCreditScore() : 100;

            // 风险评估
            int riskScore = 0; // 风险分数，越高越危险
            StringBuilder warnings = new StringBuilder();

            // 检查信用分
            if (buyerCredit < 80) {
                riskScore += 30;
                warnings.append("⚠️ 买家信用较低（").append(buyerCredit).append("分）\n");
            }
            if (sellerCredit < 80) {
                riskScore += 30;
                warnings.append("⚠️卖家信用较低（").append(sellerCredit).append("分）\n");
            }

            // 检查交易金额
            if (price != null && price > 1000) {
                riskScore += 10;
                warnings.append("⚠️大额交易，建议优先选择面交\n");
            }

            // 风险等级
            String riskLevel;
            String advice;

            if (riskScore == 0) {
                riskLevel = "低风险";
                advice = "交易安全，可以放心进行";
            } else if (riskScore <= 30) {
                riskLevel = "中等风险";
                advice = "建议使用平台担保交易，优先选择面交";
            } else {
                riskLevel = "高风险";
                advice = "建议谨慎交易，必须使用平台担保，优先面交";
            }

            return String.format("""
                    交易风险评估结果：

                    风险等级：%s
                    风险分数：%d

                    买家信用：%d分
                    卖家信用：%d分
                    交易金额：¥%.2f

                    %s

                    建议：%s
                    """,
                    riskLevel,
                    riskScore,
                    buyerCredit,
                    sellerCredit,
                    price != null ? price : 0,
                    warnings.length() > 0 ? warnings.toString() : "无特殊风险",
                    advice
            );

        } catch (Exception e) {
            log.error("评估交易风险失败", e);
            return "评估交易风险失败：" + e.getMessage();
        }
    }
}
