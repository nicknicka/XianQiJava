package com.xx.xianqijava.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.config.CreditScoreConfig;
import com.xx.xianqijava.entity.CreditRecord;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserCreditExt;
import com.xx.xianqijava.mapper.CreditRecordMapper;
import com.xx.xianqijava.mapper.UserCreditExtMapper;
import com.xx.xianqijava.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 信用分衰减定时任务
 */
@Slf4j
@Component
public class CreditScoreDecayTask {

    @Autowired
    private CreditScoreConfig config;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditRecordMapper creditRecordMapper;

    @Autowired
    private UserCreditExtMapper userCreditExtMapper;

    /**
     * 每天凌晨2点执行衰减检查（使用扩展表）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void decayInactiveUserScores() {
        log.info("开始执行信用分衰减任务");

        try {
            // 从扩展表获取需要衰减的用户
            List<UserCreditExt> usersForDecay = userCreditExtMapper.getUsersForDecay();

            log.info("找到 {} 个需要衰减的用户", usersForDecay.size());

            int totalProcessed = 0;
            for (UserCreditExt creditExt : usersForDecay) {
                try {
                    // 获取完整的用户信息
                    User user = userMapper.selectById(creditExt.getUserId());
                    if (user != null) {
                        processUserDecay(user, creditExt);
                        totalProcessed++;
                    }
                } catch (Exception e) {
                    log.error("处理用户 {} 信用分衰减失败", creditExt.getUserId(), e);
                }
            }

            log.info("信用分衰减任务完成，共处理 {} 个用户", totalProcessed);

        } catch (Exception e) {
            log.error("信用分衰减任务执行失败", e);
        }
    }

    /**
     * 处理单个用户的信用分衰减
     */
    private void processUserDecay(User user, UserCreditExt creditExt) {
        int currentScore = user.getCreditScore();

        // 只对高于最低分且未达到衰减时间的用户进行衰减
        if (currentScore <= config.getDecay().getDecayMin()) {
            return;
        }

        // 计算不活跃天数
        long inactiveDays = ChronoUnit.DAYS.between(
            creditExt.getLastActiveTime(),
            LocalDateTime.now()
        );

        // 计算衰减周期数
        int decayPeriods = (int) (inactiveDays / 30);

        // 检查是否已经衰减过
        if (creditExt.getLastCreditDecayTime() != null) {
            long daysSinceLastDecay = ChronoUnit.DAYS.between(
                creditExt.getLastCreditDecayTime(),
                LocalDateTime.now()
            );

            if (daysSinceLastDecay < 30) {
                // 不足30天，不需要再次衰减
                return;
            }

            // 更新衰减周期数（基于上次衰减时间）
            decayPeriods = (int) (daysSinceLastDecay / 30);
        }

        if (decayPeriods == 0) {
            return;
        }

        // 计算总衰减分数
        double totalDecay = decayPeriods * config.getDecay().getDecayRate();
        int newScore = (int) Math.max(
            currentScore - totalDecay,
            config.getDecay().getDecayMin()
        );

        if (newScore < currentScore) {
            String newCreditLevel = calculateCreditLevel(newScore);

            // 更新用户信用分（原表）
            user.setCreditScore(newScore);
            userMapper.updateById(user);

            // 更新扩展表
            userCreditExtMapper.updateCreditLevel(
                Long.parseLong(user.getId()),
                newCreditLevel
            );

            // 更新衰减时间
            userCreditExtMapper.updateDecayTime(
                Long.parseLong(user.getId()),
                LocalDateTime.now()
            );

            // 记录衰减
            CreditRecord record = new CreditRecord();
            record.setUserId(user.getId());
            record.setScoreBefore(currentScore);
            record.setScoreChange(BigDecimal.valueOf(newScore - currentScore));
            record.setScoreAfter(newScore);
            record.setReason(CreditRecord.Reason.DECAY.getCode());
            record.setReasonDetail("长期不活跃衰减，不活跃天数: " + inactiveDays);
            record.setCreatedAt(LocalDateTime.now());
            creditRecordMapper.insert(record);

            log.info("用户 {} 信用分从 {} 衰减至 {}，不活跃天数: {}",
                user.getId(), currentScore, newScore, inactiveDays);
        }
    }

    /**
     * 计算信用等级
     */
    private String calculateCreditLevel(Integer score) {
        if (score >= 90) return "excellent";
        if (score >= 80) return "good";
        if (score >= 70) return "normal";
        return "poor";
    }
}
