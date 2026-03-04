package com.xx.xianqijava.initializer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.entity.FlashSaleSessionTemplate;
import com.xx.xianqijava.mapper.FlashSaleSessionMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 秒杀场次初始化器
 * 在应用启动时检查并初始化必要的秒杀数据
 */
@Component
@Order(10)  // 在 SystemInitializer 之后执行
@RequiredArgsConstructor
public class FlashSaleInitializer extends BaseInitializer {

    private final FlashSaleSessionMapper sessionMapper;
    private final FlashSaleSessionTemplateMapper templateMapper;

    @Override
    protected String getName() {
        return "秒杀场次管理";
    }

    @Override
    protected void doInit() {
        // 1. 初始化场次模板
        initSessionTemplates();

        // 2. 初始化固定场次
        initFixedSessions();

        // 3. 更新今天的场次时间
        updateTodaySessions();
    }

    /**
     * 初始化场次模板
     */
    private void initSessionTemplates() {
        // 检查是否已有模板
        Long templateCount = templateMapper.selectCount(null);

        if (templateCount > 0) {
            log.info("场次模板已存在，跳过初始化。当前模板数：{}", templateCount);
            return;
        }

        log.info("初始化场次模板...");

        // 创建4个默认模板
        FlashSaleSessionTemplate[] templates = {
            createTemplate("早场", "08:00", 2, 1),
            createTemplate("午场", "12:00", 2, 2),
            createTemplate("晚场", "18:00", 2, 3),
            createTemplate("夜场", "22:00", 2, 4)
        };

        for (FlashSaleSessionTemplate template : templates) {
            templateMapper.insert(template);
            log.info("创建场次模板：{}", template.getName());
        }
    }

    /**
     * 初始化固定场次
     */
    private void initFixedSessions() {
        // 检查是否已有固定场次
        Long sessionCount = sessionMapper.selectCount(
            new LambdaQueryWrapper<FlashSaleSession>()
                .eq(FlashSaleSession::getSessionType, 1)
        );

        if (sessionCount > 0) {
            log.info("固定场次已存在，跳过初始化。当前场次数：{}", sessionCount);
            return;
        }

        log.info("初始化固定场次...");

        LocalDate today = LocalDate.now();

        // 创建4个固定场次
        FlashSaleSession[] sessions = {
            createSession(1L, "早场", "08:00", today.atTime(LocalTime.of(8, 0)), today.atTime(LocalTime.of(10, 0)), 1),
            createSession(2L, "午场", "12:00", today.atTime(LocalTime.of(12, 0)), today.atTime(LocalTime.of(14, 0)), 2),
            createSession(3L, "晚场", "18:00", today.atTime(LocalTime.of(18, 0)), today.atTime(LocalTime.of(20, 0)), 3),
            createSession(4L, "夜场", "22:00", today.atTime(LocalTime.of(22, 0)), today.plusDays(1).atTime(LocalTime.of(0, 0)), 4)
        };

        for (FlashSaleSession session : sessions) {
            sessionMapper.insert(session);
            log.info("创建固定场次：{} ({})", session.getName(), session.getSessionTime());
        }
    }

    /**
     * 更新今天的场次时间
     * 确保固定场次的日期是最新的
     */
    private void updateTodaySessions() {
        LocalDate today = LocalDate.now();

        // 查询所有固定场次
        java.util.List<FlashSaleSession> sessions = sessionMapper.selectList(
            new LambdaQueryWrapper<FlashSaleSession>()
                .eq(FlashSaleSession::getSessionType, 1)
                .orderByAsc(FlashSaleSession::getSessionId)
        );

        for (FlashSaleSession session : sessions) {
            LocalTime time = LocalTime.parse(session.getSessionTime());
            LocalDateTime startTime = LocalDateTime.of(today, time);
            LocalDateTime endTime = startTime.plusHours(2);

            // 夜场跨天处理
            if (time.isAfter(LocalTime.of(22, 0))) {
                endTime = startTime.plusHours(2);
            }

            // 只在日期不是今天时才更新
            if (!session.getStartTime().toLocalDate().equals(today)) {
                session.setStartTime(startTime);
                session.setEndTime(endTime);
                sessionMapper.updateById(session);
                log.info("更新场次日期：{} -> {}", session.getSessionTime(), today);
            }
        }
    }

    /**
     * 创建场次模板对象
     */
    private FlashSaleSessionTemplate createTemplate(String name, String time, int duration, int sortOrder) {
        FlashSaleSessionTemplate template = new FlashSaleSessionTemplate();
        template.setName(name);
        template.setSessionTime(time);
        template.setDurationHours(duration);
        template.setSortOrder(sortOrder);
        template.setIsEnabled(1);
        return template;
    }

    /**
     * 创建场次对象
     */
    private FlashSaleSession createSession(Long sessionId, String name, String time,
                                           LocalDateTime startTime, LocalDateTime endTime, int sortOrder) {
        FlashSaleSession session = new FlashSaleSession();
        session.setSessionId(sessionId);
        session.setName(name);
        session.setSessionTime(time);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setSortOrder(sortOrder);
        session.setSessionType(1);  // 固定场次
        session.setEnabled(1);       // 启用
        return session;
    }
}
