package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.RecommendationService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐系统控制器
 */
@Slf4j
@Tag(name = "智能推荐系统")
@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 获取个性化推荐商品
     */
    @GetMapping("/personalized")
    @Operation(summary = "获取个性化推荐商品")
    public Result<List<ProductVO>> getPersonalizedRecommendations(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取个性化推荐, userId={}, limit={}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getPersonalizedRecommendations(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 获取热门商品推荐
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门商品推荐")
    public Result<List<ProductVO>> getHotProducts(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门商品推荐, categoryId={}, limit={}", categoryId, limit);
        List<ProductVO> recommendations = recommendationService.getHotProducts(categoryId, limit);
        return Result.success(recommendations);
    }

    /**
     * 获取新品推荐
     */
    @GetMapping("/new")
    @Operation(summary = "获取新品推荐")
    public Result<List<ProductVO>> getNewProducts(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取新品推荐, limit={}", limit);
        List<ProductVO> recommendations = recommendationService.getNewProducts(limit);
        return Result.success(recommendations);
    }

    /**
     * 基于浏览历史的推荐
     */
    @GetMapping("/by-history")
    @Operation(summary = "基于浏览历史推荐")
    public Result<List<ProductVO>> getRecommendationsByHistory(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("基于浏览历史推荐, userId={}, limit={}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByHistory(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 基于收藏的推荐
     */
    @GetMapping("/by-favorites")
    @Operation(summary = "基于收藏推荐")
    public Result<List<ProductVO>> getRecommendationsByFavorites(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("基于收藏推荐, userId={}, limit={}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByFavorites(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 协同过滤推荐
     */
    @GetMapping("/collaborative")
    @Operation(summary = "协同过滤推荐")
    public Result<List<ProductVO>> getRecommendationsByCollaborative(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("协同过滤推荐, userId={}, limit={}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByCollaborative(userId, limit);
        return Result.success(recommendations);
    }
}
