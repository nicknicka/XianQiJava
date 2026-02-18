package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.EvaluationCreateDTO;
import com.xx.xianqijava.entity.Evaluation;
import com.xx.xianqijava.service.EvaluationService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.EvaluationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
@Tag(name = "评价管理", description = "评价相关接口")
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * 创建评价
     */
    @PostMapping
    @Operation(summary = "创建评价")
    public Result<EvaluationVO> createEvaluation(@Valid @RequestBody EvaluationCreateDTO createDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建评价, userId={}, orderId={}, rating={}", userId, createDTO.getOrderId(), createDTO.getRating());
        EvaluationVO evaluationVO = evaluationService.createEvaluation(createDTO, userId);
        return Result.success("评价创建成功", evaluationVO);
    }

    /**
     * 获取订单的评价列表
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "获取订单的评价列表")
    public Result<IPage<EvaluationVO>> getOrderEvaluations(
            @Parameter(description = "订单ID") @PathVariable("orderId") Long orderId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        Page<Evaluation> pageParam = new Page<>(page, size);
        IPage<EvaluationVO> evaluationVOPage = evaluationService.getOrderEvaluations(orderId, pageParam);
        return Result.success(evaluationVOPage);
    }

    /**
     * 获取用户的评价列表（作为被评价人）
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的评价列表")
    public Result<IPage<EvaluationVO>> getUserEvaluations(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        Page<Evaluation> pageParam = new Page<>(page, size);
        IPage<EvaluationVO> evaluationVOPage = evaluationService.getUserEvaluations(userId, pageParam);
        return Result.success(evaluationVOPage);
    }

    /**
     * 获取用户的平均评分
     */
    @GetMapping("/user/{userId}/average-rating")
    @Operation(summary = "获取用户的平均评分")
    public Result<Integer> getUserAverageRating(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        Integer avgRating = evaluationService.getUserAverageRating(userId);
        return Result.success(avgRating);
    }
}
