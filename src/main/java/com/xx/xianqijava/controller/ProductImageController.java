package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.ProductImageCreateDTO;
import com.xx.xianqijava.service.ProductImageService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ProductImageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品图片控制器
 */
@Slf4j
@Tag(name = "商品图片管理")
@RestController
@RequestMapping("/product/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * 添加商品图片
     */
    @Operation(summary = "添加商品图片")
    @PostMapping
    public Result<ProductImageVO> addProductImage(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductImageCreateDTO imageCreateDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("添加商品图片, productId={}, userId={}", productId, userId);
        ProductImageVO imageVO = productImageService.addProductImage(productId, imageCreateDTO, userId);
        return Result.success("图片添加成功", imageVO);
    }

    /**
     * 获取商品图片列表
     */
    @Operation(summary = "获取商品图片列表")
    @GetMapping
    public Result<List<ProductImageVO>> getProductImages(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId) {
        log.info("查询商品图片列表, productId={}", productId);
        List<ProductImageVO> images = productImageService.getProductImages(productId);
        return Result.success(images);
    }

    /**
     * 删除商品图片
     */
    @Operation(summary = "删除商品图片")
    @DeleteMapping("/{imageId}")
    public Result<Void> deleteProductImage(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId,
            @Parameter(description = "图片ID") @PathVariable("imageId") Long imageId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除商品图片, imageId={}, userId={}", imageId, userId);
        productImageService.deleteProductImage(imageId, userId);
        return Result.success("图片删除成功");
    }

    /**
     * 设置封面图
     */
    @Operation(summary = "设置封面图")
    @PutMapping("/{imageId}/cover")
    public Result<Void> setCoverImage(
            @Parameter(description = "商品ID") @PathVariable("productId") Long productId,
            @Parameter(description = "图片ID") @PathVariable("imageId") Long imageId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("设置封面图, imageId={}, userId={}", imageId, userId);
        productImageService.setCoverImage(imageId, userId);
        return Result.success("封面图设置成功");
    }
}
