package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.ProductAuditDTO;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ProductAuditVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 商品审核控制器（管理员）
 */
@Slf4j
@Tag(name = "商品审核管理（管理员）")
@RestController
@RequestMapping("/api/admin/product-audit")
@RequiredArgsConstructor
public class ProductAuditController {

    private final ProductService productService;

    /**
     * 审核商品
     */
    @PutMapping("/audit")
    @Operation(summary = "审核商品")
    public Result<ProductAuditVO> auditProduct(@Valid @RequestBody ProductAuditDTO auditDTO) {
        Long auditorId = SecurityUtil.getCurrentUserIdRequired();
        log.info("审核商品, productId={}, auditStatus={}, auditorId={}",
                auditDTO.getProductId(), auditDTO.getAuditStatus(), auditorId);
        ProductAuditVO auditVO = productService.auditProduct(auditDTO, auditorId);
        return Result.success(auditVO);
    }

    /**
     * 获取待审核商品列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待审核商品列表")
    public Result<IPage<ProductAuditVO>> getPendingProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询待审核商品列表, page={}", current);

        Page<Product> page = new Page<>(current, size);
        IPage<ProductAuditVO> result = productService.getPendingProducts(page);
        return Result.success(result);
    }

    /**
     * 获取所有商品审核列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有商品审核列表")
    public Result<IPage<ProductAuditVO>> getAllProductAudits(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "审核状态筛选") @RequestParam(required = false) Integer auditStatus) {
        log.info("查询所有商品审核列表, page={}, auditStatus={}", current, auditStatus);

        Page<Product> page = new Page<>(current, size);
        IPage<ProductAuditVO> result = productService.getAllProductAudits(page, auditStatus);
        return Result.success(result);
    }

    /**
     * 获取商品审核详情
     */
    @GetMapping("/{productId}")
    @Operation(summary = "获取商品审核详情")
    public Result<ProductAuditVO> getProductAuditDetail(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        log.info("查询商品审核详情, productId={}", productId);
        ProductAuditVO auditVO = productService.getProductAuditDetail(productId);
        return Result.success(auditVO);
    }
}
