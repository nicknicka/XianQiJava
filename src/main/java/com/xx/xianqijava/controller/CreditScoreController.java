package com.xx.xianqijava.controller;

import com.xx.xianqijava.dto.CreditDetailVO;
import com.xx.xianqijava.service.CreditScoreService;
import com.xx.xianqijava.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 信用分相关接口
 */
@Slf4j
@Tag(name = "信用分管理")
@RestController
@RequestMapping("/credit")
public class CreditScoreController {

    @Autowired
    private CreditScoreService creditScoreService;

    /**
     * 获取信用分详情
     */
    @Operation(summary = "获取信用分详情")
    @GetMapping("/detail/{userId}")
    public Result<CreditDetailVO> getCreditDetail(
        @Parameter(description = "用户ID") @PathVariable String userId) {
        try {
            CreditDetailVO detail = creditScoreService.getCreditDetail(userId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取信用分详情失败", e);
            return Result.error("获取信用分详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的信用分详情
     */
    @Operation(summary = "获取当前用户的信用分详情")
    @GetMapping("/my")
    public Result<CreditDetailVO> getMyCreditDetail(
        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                return Result.error("用户未登录");
            }
            CreditDetailVO detail = creditScoreService.getCreditDetail(userId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取信用分详情失败", e);
            return Result.error("获取信用分详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取信用分规则说明
     */
    @Operation(summary = "获取信用分规则说明")
    @GetMapping("/rules")
    public Result<CreditRulesVO> getCreditRules() {
        try {
            CreditRulesVO rules = new CreditRulesVO();
            rules.setInitialScore(60);
            rules.setScoreRange("0-100");
            rules.setDailyMaxGain(3.0);
            rules.setWeeklyMaxGain(15.0);
            rules.setMonthlyMaxGain(50.0);

            // 加分规则
            rules.setGainRules(List.of(
                new RuleItem("完成交易", "根据交易金额获得0.5-2分", "每日最多+3分", "+0.5~+2"),
                new RuleItem("收到好评", "基础1分，评价质量可额外加0.5分", "每日最多+3分", "+1~+2"),
                new RuleItem("实名认证", "完成实名认证", "一次性加分", "+5"),
                new RuleItem("学生认证", "完成学生认证", "一次性加分", "+3"),
                new RuleItem("手机绑定", "绑定手机号", "一次性加分", "+1"),
                new RuleItem("举报被采纳", "举报违规并被采纳", "每月上限+5分", "+2")
            ));

            // 减分规则
            rules.setLossRules(List.of(
                new RuleItem("收到差评", "收到1-2星差评", "不受上限限制", "-3~-5"),
                new RuleItem("收到中评", "收到3星中评", "不受上限限制", "-1"),
                new RuleItem("取消订单", "无正当理由取消订单", "每次-2分", "-2"),
                new RuleItem("举报成立", "被举报并确认违规", "-10分", "-10"),
                new RuleItem("违规惩罚", "严重违反平台规则", "-20分", "-20"),
                new RuleItem("商品下架", "商品因违规被下架", "-5分", "-5")
            ));

            // 衰减规则
            rules.setDecayRule(new DecayRuleItem(
                "长期不活跃衰减",
                "90天不活跃开始衰减，每30天衰减0.5分",
                "最低衰减到60分"
            ));

            return Result.success(rules);
        } catch (Exception e) {
            log.error("获取信用分规则失败", e);
            return Result.error("获取信用分规则失败: " + e.getMessage());
        }
    }

    /**
     * 信用分规则视图对象
     */
    @lombok.Data
    public static class CreditRulesVO {
        private Integer initialScore;
        private String scoreRange;
        private Double dailyMaxGain;
        private Double weeklyMaxGain;
        private Double monthlyMaxGain;
        private java.util.List<RuleItem> gainRules;
        private java.util.List<RuleItem> lossRules;
        private DecayRuleItem decayRule;
    }

    /**
     * 规则项
     */
    @lombok.Data
    public static class RuleItem {
        private String title;
        private String description;
        private String limit;
        private String score;

        public RuleItem(String title, String description, String limit, String score) {
            this.title = title;
            this.description = description;
            this.limit = limit;
            this.score = score;
        }
    }

    /**
     * 衰减规则项
     */
    @lombok.Data
    public static class DecayRuleItem {
        private String title;
        private String description;
        private String limit;

        public DecayRuleItem(String title, String description, String limit) {
            this.title = title;
            this.description = description;
            this.limit = limit;
        }
    }
}
