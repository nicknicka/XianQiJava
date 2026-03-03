package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.FlashSaleService;
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
 * 秒杀功能控制器（已简化，只使用场次）
 */
@Slf4j
@Tag(name = "限时秒杀")
@RestController
@RequestMapping("/flash-sale")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    // ========== 主要接口 ==========

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
     * 获取当前秒杀商品列表（首页展示用）
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

    // ========== 购买相关接口 ==========

    /**
     * 检查用户是否可以参与秒杀
     */
    @GetMapping("/can-buy")
    @Operation(summary = "检查用户是否可以参与秒杀")
    public Result<Boolean> canBuy(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "场次ID") @RequestParam Long sessionId) {
        log.info("检查用户是否可以参与秒杀, userId={}, productId={}, sessionId={}", userId, productId, sessionId);
        boolean canBuy = flashSaleService.canBuy(userId, productId, sessionId);
        return Result.success(canBuy);
    }

    /**
     * 获取用户在某场次中的购买数量
     */
    @GetMapping("/user-buy-count")
    @Operation(summary = "获取用户购买数量")
    public Result<Integer> getUserBuyCount(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "场次ID") @RequestParam Long sessionId) {
        log.info("获取用户购买数量, userId={}, sessionId={}", userId, sessionId);
        int count = flashSaleService.getUserBuyCount(userId, sessionId);
        return Result.success(count);
    }

    // ========== 前端秒杀页面所需接口 ==========

    /**
     * 检查秒杀购买资格
     */
    @GetMapping("/{productId}/check")
    @Operation(summary = "检查秒杀购买资格")
    public Result<java.util.Map<String, Object>> checkSeckillEligibility(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Long userId = com.xx.xianqijava.util.SecurityUtil.getCurrentUserId();
        log.info("检查秒杀购买资格, productId={}, userId={}", productId, userId);

        java.util.Map<String, Object> result = new java.util.HashMap<>();

        // 未登录用户默认允许（点击购买时再提示登录）
        if (userId == null) {
            result.put("canBuy", true);
            return Result.success(result);
        }

        boolean canBuy = flashSaleService.checkSeckillEligibility(userId, productId);
        result.put("canBuy", canBuy);

        if (!canBuy) {
            result.put("reason", flashSaleService.getCannotBuyReason(userId, productId));
        } else {
            result.put("remainingStock", flashSaleService.getRemainingStock(productId));
            result.put("userBuyLimit", flashSaleService.getUserBuyLimit(productId));
        }

        return Result.success(result);
    }

    /**
     * 秒杀抢购
     */
    @PostMapping("/{productId}/buy")
    @Operation(summary = "秒杀抢购")
    public Result<java.util.Map<String, Object>> seckillBuy(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @RequestBody java.util.Map<String, Object> params) {
        Long userId = com.xx.xianqijava.util.SecurityUtil.getCurrentUserIdRequired();
        log.info("秒杀抢购, productId={}, userId={}", productId, userId);

        Integer quantity = params.get("quantity") != null ?
            Integer.valueOf(params.get("quantity").toString()) : 1;
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";

        java.util.Map<String, Object> result = flashSaleService.seckillBuy(userId, productId, quantity, remark);
        return Result.success("抢购成功", result);
    }
}
