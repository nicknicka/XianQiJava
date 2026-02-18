package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.service.ProductFavoriteService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 商品收藏控制器
 */
@Slf4j
@Tag(name = "商品收藏管理")
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class ProductFavoriteController {

    private final ProductFavoriteService productFavoriteService;

    /**
     * 添加收藏
     */
    @Operation(summary = "添加收藏")
    @PostMapping("/{productId}")
    public Result<Void> addFavorite(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("添加收藏, userId={}, productId={}", userId, productId);
        productFavoriteService.addFavorite(userId, productId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/{productId}")
    public Result<Void> removeFavorite(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("取消收藏, userId={}, productId={}", userId, productId);
        productFavoriteService.removeFavorite(userId, productId);
        return Result.success("取消收藏成功");
    }

    /**
     * 检查是否已收藏
     */
    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{productId}")
    public Result<Boolean> checkFavorite(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean isFavorited = productFavoriteService.isFavorited(userId, productId);
        return Result.success(isFavorited);
    }

    /**
     * 我的收藏列表
     */
    @Operation(summary = "我的收藏列表")
    @GetMapping
    public Result<IPage<ProductVO>> getFavoriteList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询收藏列表, userId={}, page={}, size={}", userId, page, size);

        Page<ProductFavorite> pageParam = new Page<>(page, size);
        IPage<ProductVO> productPage = productFavoriteService.getFavoriteList(userId, pageParam);

        return Result.success(productPage);
    }
}
