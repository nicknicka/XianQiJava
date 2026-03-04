package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.entity.FlashSaleSessionTemplate;
import com.xx.xianqijava.mapper.FlashSaleSessionMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionTemplateMapper;
import com.xx.xianqijava.task.FlashSaleSessionScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀管理后台接口
 */
@Slf4j
@Tag(name = "秒杀管理后台")
@RestController
@RequestMapping("/flash-sale/admin")
@RequiredArgsConstructor
public class FlashSaleAdminController {

    private final FlashSaleSessionTemplateMapper templateMapper;
    private final FlashSaleSessionMapper sessionMapper;
    private final FlashSaleSessionScheduler scheduler;

    // ========== 模板管理 ==========

    /**
     * 获取所有场次模板
     */
    @GetMapping("/templates")
    @Operation(summary = "获取场次模板列表")
    public Result<List<FlashSaleSessionTemplate>> getTemplates() {
        LambdaQueryWrapper<FlashSaleSessionTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FlashSaleSessionTemplate::getSortOrder);
        List<FlashSaleSessionTemplate> templates = templateMapper.selectList(wrapper);
        return Result.success(templates);
    }

    /**
     * 创建/更新场次模板
     */
    @PostMapping("/template")
    @Operation(summary = "保存场次模板")
    public Result<String> saveTemplate(@RequestBody FlashSaleSessionTemplate template) {
        if (template.getTemplateId() == null) {
            templateMapper.insert(template);
            log.info("创建场次模板：{}", template.getName());
        } else {
            templateMapper.updateById(template);
            log.info("更新场次模板：{}", template.getName());
        }
        return Result.success("保存成功");
    }

    /**
     * 启用/禁用模板
     */
    @PutMapping("/template/{templateId}/toggle")
    @Operation(summary = "启用/禁用模板")
    public Result<String> toggleTemplate(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        FlashSaleSessionTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            return Result.error("模板不存在");
        }

        template.setIsEnabled(template.getIsEnabled() == 1 ? 0 : 1);
        templateMapper.updateById(template);

        log.info("{}场次模板：{}", template.getIsEnabled() == 1 ? "启用" : "禁用", template.getName());
        return Result.success("操作成功");
    }

    // ========== 场次生成 ==========

    /**
     * 手动触发更新今天的场次
     */
    @PostMapping("/sessions/generate")
    @Operation(summary = "手动更新今天的场次")
    public Result<String> generateTodaySessions() {
        scheduler.updateDailySessions();
        return Result.success("更新任务已触发");
    }

    /**
     * 为指定日期更新场次
     */
    @PostMapping("/sessions/generate/{date}")
    @Operation(summary = "为指定日期更新场次")
    public Result<String> generateSessionsForDate(
            @Parameter(description = "日期（yyyy-MM-dd）") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        scheduler.updateSessionsForDate(date);
        return Result.success("更新任务已触发，日期：" + date);
    }

    // ========== 临时场次管理 ==========

    /**
     * 获取所有临时场次
     */
    @GetMapping("/sessions/temporary")
    @Operation(summary = "获取临时场次列表")
    public Result<List<FlashSaleSession>> getTemporarySessions() {
        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleSession::getSessionType, 2)  // 临时场次
               .orderByAsc(FlashSaleSession::getStartTime);
        List<FlashSaleSession> sessions = sessionMapper.selectList(wrapper);
        return Result.success(sessions);
    }

    /**
     * 创建临时场次
     */
    @PostMapping("/sessions/temporary")
    @Operation(summary = "创建临时场次")
    public Result<String> createTemporarySession(@RequestBody FlashSaleSession session) {
        // 验证必填字段
        if (session.getName() == null || session.getName().trim().isEmpty()) {
            return Result.error("场次名称不能为空");
        }
        if (session.getStartTime() == null) {
            return Result.error("开始时间不能为空");
        }
        if (session.getEndTime() == null) {
            return Result.error("结束时间不能为空");
        }
        if (session.getStartTime().isAfter(session.getEndTime())) {
            return Result.error("开始时间不能晚于结束时间");
        }

        // 设置为临时场次
        session.setSessionType(2);

        // 自动设置 session_time（从 startTime 提取时间部分）
        if (session.getSessionTime() == null || session.getSessionTime().isEmpty()) {
            session.setSessionTime(session.getStartTime().toLocalTime().toString());
        }

        // 如果没设置排序，放在最后
        if (session.getSortOrder() == null) {
            session.setSortOrder(99);
        }

        sessionMapper.insert(session);
        log.info("创建临时场次：{} ({} - {})", session.getName(), session.getStartTime(), session.getEndTime());

        return Result.success("临时场次创建成功");
    }

    /**
     * 更新临时场次
     */
    @PutMapping("/sessions/temporary/{sessionId}")
    @Operation(summary = "更新临时场次")
    public Result<String> updateTemporarySession(
            @Parameter(description = "场次ID") @PathVariable Long sessionId,
            @RequestBody FlashSaleSession session) {
        FlashSaleSession existing = sessionMapper.selectById(sessionId);
        if (existing == null) {
            return Result.error("场次不存在");
        }

        // 只允许更新临时场次
        if (existing.getSessionType() != 2) {
            return Result.error("只能更新临时场次");
        }

        session.setSessionId(sessionId);
        session.setSessionType(2);  // 确保类型不变
        sessionMapper.updateById(session);

        log.info("更新临时场次：{}", session.getName());
        return Result.success("更新成功");
    }

    /**
     * 删除临时场次
     */
    @DeleteMapping("/sessions/temporary/{sessionId}")
    @Operation(summary = "删除临时场次")
    public Result<String> deleteTemporarySession(
            @Parameter(description = "场次ID") @PathVariable Long sessionId) {
        FlashSaleSession existing = sessionMapper.selectById(sessionId);
        if (existing == null) {
            return Result.error("场次不存在");
        }

        // 只允许删除临时场次
        if (existing.getSessionType() != 2) {
            return Result.error("只能删除临时场次，不能删除固定场次");
        }

        sessionMapper.deleteById(sessionId);
        log.info("删除临时场次：{}", existing.getName());

        return Result.success("删除成功");
    }

    // ========== 固定场次管理 ==========

    /**
     * 获取所有固定场次
     */
    @GetMapping("/sessions/fixed")
    @Operation(summary = "获取固定场次列表")
    public Result<List<FlashSaleSession>> getFixedSessions() {
        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleSession::getSessionType, 1)  // 固定场次
               .orderByAsc(FlashSaleSession::getSessionId);
        List<FlashSaleSession> sessions = sessionMapper.selectList(wrapper);
        return Result.success(sessions);
    }

    /**
     * 启用/禁用固定场次
     */
    @PutMapping("/sessions/{sessionId}/toggle")
    @Operation(summary = "启用/禁用场次")
    public Result<String> toggleSession(
            @Parameter(description = "场次ID") @PathVariable Long sessionId) {
        FlashSaleSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return Result.error("场次不存在");
        }

        // 切换启用状态
        session.setEnabled(session.getEnabled() == null || session.getEnabled() == 1 ? 0 : 1);
        sessionMapper.updateById(session);

        String status = session.getEnabled() == 1 ? "启用" : "禁用";
        log.info("{}场次：{}", status, session.getName());

        return Result.success("场次已" + status);
    }

    /**
     * 批量启用/禁用所有固定场次
     */
    @PutMapping("/sessions/batch-toggle")
    @Operation(summary = "批量启用/禁用所有固定场次")
    public Result<String> batchToggleSessions(@RequestBody java.util.Map<String, Integer> params) {
        Integer enabled = params.get("enabled");
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            return Result.error("参数错误：enabled 必须是 0 或 1");
        }

        LambdaUpdateWrapper<FlashSaleSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FlashSaleSession::getSessionType, 1)  // 只更新固定场次
               .set(FlashSaleSession::getEnabled, enabled);

        int updated = sessionMapper.update(null, wrapper);

        String status = enabled == 1 ? "启用" : "禁用";
        log.info("批量{}了 {} 个固定场次", status, updated);

        return Result.success("已" + status + " " + updated + " 个固定场次");
    }
}
