package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.FlashSaleService;
import com.xx.xianqijava.vo.FlashSaleActivityVO;
import com.xx.xianqijava.vo.FlashSaleProductVO;
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
}
