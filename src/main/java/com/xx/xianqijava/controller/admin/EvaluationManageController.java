package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Evaluation;
import com.xx.xianqijava.service.EvaluationService;
import com.xx.xianqijava.vo.EvaluationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价管理控制器 - 管理端
 */
@Slf4j
@RestController
@RequestMapping("/admin/evaluation")
@RequiredArgsConstructor
@Tag(name = "评价管理")
public class EvaluationManageController {

    private final EvaluationService evaluationService;

    /**
     * 分页查询评价列表（管理员）
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询评价列表")
    public Result<IPage<EvaluationVO>> getEvaluationList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "评价人ID") @RequestParam(required = false) Long fromUserId,
            @Parameter(description = "被评价人ID") @RequestParam(required = false) Long toUserId,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "评分") @RequestParam(required = false) Integer score,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        log.info("查询评价列表, page={}, size={}, fromUserId={}, toUserId={}, score={}",
                page, size, fromUserId, toUserId, score);

        Page<Evaluation> pageParam = new Page<>(page, size);
        IPage<EvaluationVO> result = evaluationService.getEvaluationList(
                pageParam, fromUserId, toUserId, orderId, score, keyword, startTime, endTime);

        return Result.success(result);
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{evalId}")
    @Operation(summary = "获取评价详情")
    public Result<EvaluationVO> getEvaluationDetail(
            @Parameter(description = "评价ID") @PathVariable("evalId") Long evalId) {
        log.info("查询评价详情, evalId={}", evalId);
        EvaluationVO evaluationVO = evaluationService.getEvaluationDetail(evalId);
        return Result.success(evaluationVO);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{evalId}")
    @Operation(summary = "删除评价")
    public Result<Void> deleteEvaluation(
            @Parameter(description = "评价ID") @PathVariable("evalId") Long evalId) {
        log.info("删除评价, evalId={}", evalId);
        evaluationService.deleteEvaluation(evalId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除评价
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除评价")
    public Result<Integer> batchDeleteEvaluations(
            @RequestBody List<Long> evalIds) {
        log.info("批量删除评价, count={}", evalIds.size());
        int count = evaluationService.batchDeleteEvaluations(evalIds);
        return Result.success("成功删除 " + count + " 条评价", count);
    }

    /**
     * 获取评价统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取评价统计信息")
    public Result<Map<String, Object>> getStatistics() {
        log.info("查询评价统计信息");

        Map<String, Object> statistics = new HashMap<>();

        // 总数统计
        long totalCount = evaluationService.count();
        statistics.put("totalCount", totalCount);

        // 平均评分
        double avgScore = evaluationService.getAverageScore();
        statistics.put("averageScore", avgScore);

        // 评分分布统计（直接放在根级别）
        statistics.put("fiveStarCount", countByScore(5));
        statistics.put("fourStarCount", countByScore(4));
        statistics.put("threeStarCount", countByScore(3));
        statistics.put("twoStarCount", countByScore(2));
        statistics.put("oneStarCount", countByScore(1));

        // 时间统计
        LocalDateTime now = LocalDateTime.now();
        statistics.put("todayCount", countByTime(now.toLocalDate().atStartOfDay(), now));
        statistics.put("weekCount", countByTime(now.minusDays(7), now));
        statistics.put("monthCount", countByTime(now.minusDays(30), now));

        return Result.success(statistics);
    }

    /**
     * 按评分统计
     */
    private long countByScore(int score) {
        LambdaQueryWrapper<Evaluation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Evaluation::getScore, score);
        return evaluationService.count(queryWrapper);
    }

    /**
     * 按时间统计
     */
    private long countByTime(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Evaluation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Evaluation::getCreateTime, start, end);
        return evaluationService.count(queryWrapper);
    }
}
