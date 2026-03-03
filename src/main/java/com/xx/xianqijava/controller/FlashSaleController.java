package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.FlashSaleService;
import com.xx.xianqijava.vo.FlashSaleActivityVO;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.FlashSaleSessionVO;
import com.xx.xianqijava.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀功能控制器
 */
@Slf4j
@Tag(name = "限时秒杀")
@RestController
@RequestMapping("/flash-sale")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    /**
     * 获取当前秒杀活动
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前秒杀活动")
    public Result<FlashSaleActivityVO> getCurrentActivity() {
        log.info("获取当前秒杀活动");
        var activity = flashSaleService.getCurrentActivity();
        if (activity == null) {
            return Result.success(null);
        }
        // TODO: 转换为 VO
        return Result.success(null);
    }

    /**
     * 获取当前秒杀商品列表
     */
    @GetMapping("/current/products")
    @Operation(summary = "获取当前秒杀商品列表")
    public Result<List<ProductVO>> getCurrentFlashSaleProducts(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取当前秒杀商品, limit={}", limit);
        List<ProductVO> products = flashSaleService.getCurrentFlashSaleProducts(limit);
        return Result.success(products);
    }

    /**
     * 获取活动商品列表
     */
    @GetMapping("/activity/{activityId}/products")
    @Operation(summary = "获取活动商品列表")
    public Result<List<FlashSaleProductVO>> getActivityProducts(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        log.info("获取活动商品, activityId={}", activityId);
        List<FlashSaleProductVO> products = flashSaleService.getActivityProducts(activityId);
        return Result.success(products);
    }

    // ========== 新增接口 ==========

    /**
     * 获取当前可见的秒杀场次列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取秒杀场次列表")
    public Result<List<FlashSaleSessionVO>> getActiveSessions() {
        log.info("获取秒杀场次列表");
        List<FlashSaleSessionVO> sessions = flashSaleService.getActiveSessions();
        return Result.success(sessions);
    }

    /**
     * 获取指定场次的秒杀商品列表（分页）
     */
    @GetMapping("/sessions/{sessionId}/products")
    @Operation(summary = "获取场次秒杀商品列表")
    public Result<List<FlashSaleProductVO>> getSessionProducts(
            @Parameter(description = "场次ID") @PathVariable Long sessionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取场次秒杀商品, sessionId={}, page={}, pageSize={}", sessionId, page, pageSize);
        List<FlashSaleProductVO> products = flashSaleService.getSessionProducts(sessionId, page, pageSize);
        return Result.success(products);
    }

    /**
     * 获取秒杀商品详情
     */
    @GetMapping("/product/{productId}/detail")
    @Operation(summary = "获取秒杀商品详情")
    public Result<FlashSaleProductVO> getFlashSaleProductDetail(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        log.info("获取秒杀商品详情, productId={}", productId);
        FlashSaleProductVO product = flashSaleService.getFlashSaleProductDetail(productId);
        if (product == null) {
            return Result.error("商品不存在或未参与秒杀活动");
        }
        return Result.success(product);
    }

    /**
     * 检查用户是否可以参与秒杀
     */
    @GetMapping("/can-buy")
    @Operation(summary = "检查用户是否可以参与秒杀")
    public Result<Boolean> canBuy(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "活动ID") @RequestParam Long activityId) {
        log.info("检查用户是否可以参与秒杀, userId={}, productId={}, activityId={}", userId, productId, activityId);
        boolean canBuy = flashSaleService.canBuy(userId, productId, activityId);
        return Result.success(canBuy);
    }

    /**
     * 获取用户在某活动中的购买数量
     */
    @GetMapping("/user-buy-count")
    @Operation(summary = "获取用户购买数量")
    public Result<Integer> getUserBuyCount(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "活动ID") @RequestParam Long activityId) {
        log.info("获取用户购买数量, userId={}, activityId={}", userId, activityId);
        int count = flashSaleService.getUserBuyCount(userId, activityId);
        return Result.success(count);
    }
}
