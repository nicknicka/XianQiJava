package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.impl.RecommendationServiceImplV2;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐系统控制器 V2 - 优化版本
 * 支持地理位置推荐、更丰富的推荐策略
 */
@Slf4j
@Tag(name = "智能推荐系统V2")
@RestController
@RequestMapping("/recommend/v2")
@RequiredArgsConstructor
public class RecommendationControllerV2 {

    @Qualifier("recommendationServiceV2")
    private final RecommendationServiceImplV2 recommendationService;

    /**
     * 获取个性化推荐商品（支持地理位置）
     */
    @GetMapping("/personalized")
    @Operation(summary = "获取个性化推荐商品（V2）")
    public Result<List<ProductVO>> getPersonalizedRecommendations(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "用户纬度") @RequestParam(required = false) BigDecimal latitude,
            @Parameter(description = "用户经度") @RequestParam(required = false) BigDecimal longitude) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取个性化推荐V2, userId={}, limit={}, location=({}, {})",
                userId, limit, latitude, longitude);
        List<ProductVO> recommendations = recommendationService.getPersonalizedRecommendations(
                userId, limit, latitude, longitude);
        return Result.success(recommendations);
    }

    /**
     * 获取热门商品推荐
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门商品推荐（V2）")
    public Result<List<ProductVO>> getHotProducts(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门商品推荐V2, categoryId={}, limit={}", categoryId, limit);
        List<ProductVO> recommendations = recommendationService.getHotProducts(categoryId, limit);
        return Result.success(recommendations);
    }

    /**
     * 获取新品推荐
     */
    @GetMapping("/new")
    @Operation(summary = "获取新品推荐（V2）")
    public Result<List<ProductVO>> getNewProducts(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取新品推荐V2, limit={}", limit);
        List<ProductVO> recommendations = recommendationService.getNewProducts(limit);
        return Result.success(recommendations);
    }

    /**
     * 基于浏览历史的推荐
     */
    @GetMapping("/by-history")
    @Operation(summary = "基于浏览历史推荐（V2）")
    public Result<List<ProductVO>> getRecommendationsByHistory(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("基于浏览历史推荐V2, userId={}, limit={}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByHistory(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 基于收藏的推荐
     */
    @GetMapping("/by-favorites")
    @Operation(summary = "基于收藏推荐（V2）")
    public Result<List<ProductVO>> getRecommendationsByFavorites(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("基于收藏推荐V2, userId={}, limit={}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByFavorites(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 协同过滤推荐
     */
    @GetMapping("/collaborative")
    @Operation(summary = "协同过滤推荐（V2）")
    public Result<List<ProductVO>> getRecommendationsByCollaborative(
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("协同过滤推荐V2, userId={}, limit={}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByCollaborative(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 基于地理位置的推荐（新增）
     */
    @GetMapping("/by-location")
    @Operation(summary = "基于地理位置推荐（附近商品）")
    public Result<List<ProductVO>> getRecommendationsByLocation(
            @Parameter(description = "用户纬度") @RequestParam BigDecimal latitude,
            @Parameter(description = "用户经度") @RequestParam BigDecimal longitude,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("基于地理位置推荐, userId={}, limit={}, location=({}, {})",
                userId, limit, latitude, longitude);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByLocation(
                userId, limit, latitude, longitude);
        return Result.success(recommendations);
    }
}
