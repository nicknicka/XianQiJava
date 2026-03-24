package com.xx.xianqijava.agent.tools;

import com.xx.xianqijava.service.SystemConfigService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 系统相关工具集
 *
 * @author Claude
 * @since 2026-03-23
 */
@Service
public class SystemTools {

    private static final Logger log = LoggerFactory.getLogger(SystemTools.class);

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 获取系统配置
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @Tool("获取系统配置值")
    public String getSystemConfig(@P("配置键") String configKey) {
        log.info("执行工具：getSystemConfig，configKey={}", configKey);

        try {
            String value = systemConfigService.getConfigValue(configKey);
            if (value == null || value.isEmpty()) {
                return "配置项不存在：" + configKey;
            }
            return "配置值：" + value;

        } catch (Exception e) {
            log.error("获取系统配置失败：configKey={}", configKey, e);
            return "获取配置失败：" + e.getMessage();
        }
    }

    /**
     * 获取交易规则
     *
     * @return 交易规则说明
     */
    @Tool("获取平台交易规则说明")
    public String getTransactionRules() {
        log.info("执行工具：getTransactionRules");

        return """
                闲七平台交易规则：

                1. 商品发布规则
                   - 商品必须真实存在，禁止发布虚假信息
                   - 商品描述必须准确，不得夸大或隐瞒
                   - 图片必须真实，禁止使用盗图
                   - 价格合理，禁止恶意低价或高价

                2. 交易流程规则
                   - 买家下单后，卖家需在48小时内确认
                   - 确认后，买家需在7天内完成支付
                   - 支付后，卖家需在3天内发货
                   - 买家确认收货或7天后自动完成

                3. 安全交易规则
                   - 优先选择校内面交
                   - 使用平台担保交易，不要直接转账
                   - 保留交易凭证和聊天记录
                   - 发现问题及时举报

                4. 评价规则
                   - 交易完成后可互相评价
                   - 评价需客观真实，禁止恶意评价
                   - 好评可提升信用分，差评会降低

                5. 违规处理
                   - 发布违禁品：永久封号
                   - 欺诈行为：永久封号并报警
                   - 恶意骚扰：警告至封号
                   - 虚假交易：降低信用分
                """;
    }

    /**
     * 获取平台帮助信息
     *
     * @param topic 帮助主题
     * @return 帮助信息
     */
    @Tool("获取平台帮助信息")
    public String getHelpInfo(@P("帮助主题") String topic) {
        log.info("执行工具：getHelpInfo，topic={}", topic);

        if (topic == null || topic.isEmpty()) {
            return """
                    闲七平台帮助中心

                    常见问题分类：
                    1. 注册与认证
                    2. 商品发布
                    3. 交易流程
                    4. 支付与收款
                    5. 评价与信用
                    6. 安全与防骗
                    7. 举报与投诉

                    请输入具体问题或选择分类获取帮助
                    """;
        }

        // 根据主题返回相应的帮助信息
        if (topic.contains("注册") || topic.contains("认证")) {
            return """
                    注册与实名认证

                    1. 注册方式
                       - 学号注册（推荐）
                       - 手机号注册

                    2. 实名认证
                       - 需要提供真实姓名
                       - 需要上传学生证或校园卡
                       - 认证后才能发布商品
                       - 认证仅需1-2个工作日

                    3. 注意事项
                       - 学号必须真实有效
                       - 每个学号只能注册一个账号
                       - 认证信息会严格保密
                    """;
        } else if (topic.contains("发布") || topic.contains("商品")) {
            return """
                    商品发布指南

                    1. 发布步骤
                       - 点击"发布"按钮
                       - 选择商品分类
                       - 上传商品图片（最多9张）
                       - 填写商品信息
                       - 设置价格和成色
                       - 选择交易方式
                       - 提交审核

                    2. 商品信息要求
                       - 标题：简洁明了，突出重点
                       - 描述：详细真实，不少于10字
                       - 图片：清晰真实，无水印
                       - 价格：合理定价，参考市场价
                       - 成色：如实标注（1-10，10为全新）

                    3. 审核说明
                       - 审核时间：1-24小时
                       - 审核通过后自动上架
                       - 审核不通过会注明原因
                    """;
        } else if (topic.contains("交易") || topic.contains("流程")) {
            return """
                    交易流程说明

                    1. 购买流程
                       - 浏览商品，选择心仪商品
                       - 点击"我想要"联系卖家
                       - 协商交易细节
                       - 创建订单并支付
                       - 确认收货并评价

                    2. 出售流程
                       - 发布商品并通过审核
                       - 收到买家咨询
                       - 协商交易细节
                       - 确认订单并等待付款
                       - 发货或安排面交
                       - 买家确认收货后收款

                    3. 交易方式
                       - 校内面交（推荐）
                       - 快递配送
                       - 当面交易
                    """;
        } else if (topic.contains("支付") || topic.contains("收款")) {
            return """
                    支付与收款说明

                    1. 支付方式
                       - 支付宝
                       - 微信支付
                       - 余额支付

                    2. 交易担保
                       - 支付后资金由平台担保
                       - 确认收货后才打款给卖家
                       - 7天未确认自动完成

                    3. 提现说明
                       - 可随时申请提现
                       - 提现到账时间：1-3个工作日
                       - 提现手续费：免费

                    4. 退款规则
                       - 未支付订单可直接取消
                       - 已支付未发货可申请退款
                       - 已发货需与卖家协商
                    """;
        } else {
            return """
                    未找到相关帮助信息。

                    请尝试以下关键词：
                    - 注册认证
                    - 商品发布
                    - 交易流程
                    - 支付收款
                    - 安全防骗

                    或联系客服获取帮助
                    """;
        }
    }
}
