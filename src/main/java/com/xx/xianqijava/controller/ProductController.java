package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.ProductCreateDTO;
import com.xx.xianqijava.dto.ProductUpdateDTO;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 商品控制器
 */
@Slf4j
@Tag(name = "商品管理")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 创建商品
     */
    @Operation(summary = "创建商品")
    @PostMapping
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductCreateDTO createDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建商品, userId={}", userId);
        ProductVO productVO = productService.createProduct(createDTO, userId);
        return Result.success("商品创建成功", productVO);
    }

    /**
     * 获取商品详情
     */
    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(
            @Parameter(description = "商品ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取商品详情, productId={}, userId={}", id, userId);
        ProductVO productVO = productService.getProductDetail(id, userId);
        return Result.success(productVO);
    }

    /**
     * 商品列表（分页）
     */
    @Operation(summary = "商品列表")
    @GetMapping
    public Result<IPage<ProductVO>> getProductList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        log.info("查询商品列表, page={}, size={}, categoryId={}, keyword={}", page, size, categoryId, keyword);
        
        Page<Product> pageParam = new Page<>(page, size);
        IPage<ProductVO> productPage = productService.getProductList(pageParam, categoryId, keyword);
        
        return Result.success(productPage);
    }

    /**
     * 搜索商品
     */
    @Operation(summary = "搜索商品")
    @GetMapping("/search")
    public Result<IPage<ProductVO>> searchProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) BigDecimal maxPrice) {
        log.info("搜索商品, page={}, size={}, keyword={}, categoryId={}, price={}~{}", 
                 page, size, keyword, categoryId, minPrice, maxPrice);
        
        Page<Product> pageParam = new Page<>(page, size);
        IPage<ProductVO> productPage = productService.searchProducts(
                pageParam, keyword, categoryId, minPrice, maxPrice);
        
        return Result.success(productPage);
    }

    /**
     * 更新商品信息
     */
    @Operation(summary = "更新商品信息")
    @PutMapping("/{id}")
    public Result<ProductVO> updateProduct(
            @Parameter(description = "商品ID") @PathVariable("id") Long id,
            @Valid @RequestBody ProductUpdateDTO updateDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新商品信息, productId={}, userId={}", id, userId);
        ProductVO productVO = productService.updateProduct(id, updateDTO, userId);
        return Result.success("商品信息更新成功", productVO);
    }

    /**
     * 更新商品状态
     */
    @Operation(summary = "更新商品状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID") @PathVariable("id") Long id,
            @Parameter(description = "状态：0-下架 1-在售 2-已售") @RequestParam Integer status) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新商品状态, productId={}, status={}, userId={}", id, status, userId);
        productService.updateProductStatus(id, status, userId);
        return Result.success("商品状态更新成功");
    }

    /**
     * 删除商品
     */
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除商品, productId={}, userId={}", id, userId);
        productService.deleteProduct(id, userId);
        return Result.success("商品删除成功");
    }

    /**
     * 获取附近商品列表
     */
    @Operation(summary = "获取附近商品列表")
    @GetMapping("/nearby")
    public Result<IPage<ProductVO>> getNearbyProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "纬度") @RequestParam(required = false) BigDecimal latitude,
            @Parameter(description = "经度") @RequestParam(required = false) BigDecimal longitude,
            @Parameter(description = "半径（公里）") @RequestParam(defaultValue = "5") Integer radius) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取附近商品, userId={}, latitude={}, longitude={}, radius={}", userId, latitude, longitude, radius);

        Page<Product> pageParam = new Page<>(page, size);
        IPage<ProductVO> productPage = productService.getNearbyProducts(pageParam, userId, latitude, longitude, radius);

        return Result.success(productPage);
    }
}
