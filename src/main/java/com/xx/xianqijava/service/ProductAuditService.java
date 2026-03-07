package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.ProductAuditDTO;
import com.xx.xianqijava.dto.admin.ProductAuditQueryDTO;
import com.xx.xianqijava.vo.admin.ProductAuditVO;

/**
 * 商品审核服务接口 - 管理端
 */
public interface ProductAuditService {

    /**
     * 分页查询商品审核列表
     *
     * @param queryDTO 查询条件
     * @return 商品审核分页数据
     */
    Page<ProductAuditVO> getProductAuditList(ProductAuditQueryDTO queryDTO);

    /**
     * 获取待审核商品列表
     *
     * @param queryDTO 查询条件
     * @return 待审核商品分页数据
     */
    Page<ProductAuditVO> getPendingAuditList(ProductAuditQueryDTO queryDTO);

    /**
     * 获取商品审核详情
     *
     * @param productId 商品ID
     * @return 商品审核详情
     */
    ProductAuditVO getProductAuditDetail(Long productId);

    /**
     * 审核商品（通过/拒绝）
     *
     * @param auditDTO 审核DTO
     * @return 是否成功
     */
    Boolean auditProduct(ProductAuditDTO auditDTO);

    /**
     * 获取商品审核统计信息
     *
     * @return 统计信息
     */
    ProductAuditStatistics getAuditStatistics();
}
