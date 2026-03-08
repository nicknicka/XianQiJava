package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.ProductAuditDTO;
import com.xx.xianqijava.dto.admin.ProductAuditQueryDTO;
import com.xx.xianqijava.service.ProductAuditService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.admin.ProductAuditStatistics;
import com.xx.xianqijava.vo.admin.ProductAuditVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 商品审核控制器 - 管理端
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/product-audit")
@Tag(name = "商品审核", description = "商品审核相关接口")
@SecurityRequirement(name = "bearer-auth")
public class ProductAuditController {

    private final ProductAuditService productAuditService;

    /**
     * 分页查询商品审核列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询商品审核列表", description = "支持多种条件筛选和排序")
    public Page<ProductAuditVO> getProductAuditList(ProductAuditQueryDTO queryDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}查询商品审核列表，查询条件：{}", adminId, queryDTO);
        return productAuditService.getProductAuditList(queryDTO);
    }

    /**
     * 分页查询待审核商品列表
     */
    @GetMapping("/pending")
    @Operation(summary = "分页查询待审核商品列表", description = "只返回待审核状态的商品")
    public Page<ProductAuditVO> getPendingAuditList(ProductAuditQueryDTO queryDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}查询待审核商品列表，查询条件：{}", adminId, queryDTO);
        return productAuditService.getPendingAuditList(queryDTO);
    }

    /**
     * 获取商品审核详情
     */
    @GetMapping("/{productId}")
    @Operation(summary = "获取商品审核详情", description = "根据商品ID获取审核详情")
    public ProductAuditVO getProductAuditDetail(@PathVariable Long productId) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取商品审核详情，商品ID：{}", adminId, productId);
        return productAuditService.getProductAuditDetail(productId);
    }

    /**
     * 审核商品（通过/拒绝）
     */
    @PostMapping("/audit")
    @Operation(summary = "审核商品", description = "审核通过或拒绝商品，拒绝时需填写原因")
    public Boolean auditProduct(@Valid @RequestBody ProductAuditDTO auditDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}审核商品，商品ID：{}，审核状态：{}",
                adminId, auditDTO.getProductId(), auditDTO.getAuditStatus());
        return productAuditService.auditProduct(auditDTO);
    }

    /**
     * 获取商品审核统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取商品审核统计信息", description = "获取待审核数、通过数、拒绝数等统计数据")
    public ProductAuditStatistics getAuditStatistics() {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取商品审核统计信息", adminId);
        return productAuditService.getAuditStatistics();
    }
}
