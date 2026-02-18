package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.ProductAuditDTO;
import com.xx.xianqijava.dto.ProductCreateDTO;
import com.xx.xianqijava.dto.ProductUpdateDTO;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.vo.ProductAuditVO;
import com.xx.xianqijava.vo.ProductVO;

import java.math.BigDecimal;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 创建商品
     */
    ProductVO createProduct(ProductCreateDTO createDTO, Long userId);

    /**
     * 获取商品详情
     */
    ProductVO getProductDetail(Long productId, Long userId);

    /**
     * 分页查询商品列表
     */
    IPage<ProductVO> getProductList(Page<Product> page, Integer categoryId, String keyword);

    /**
     * 搜索商品
     */
    IPage<ProductVO> searchProducts(Page<Product> page, String keyword, Integer categoryId,
                                   BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 更新商品状态
     */
    void updateProductStatus(Long productId, Integer status, Long userId);

    /**
     * 删除商品
     */
    void deleteProduct(Long productId, Long userId);

    /**
     * 更新商品信息
     */
    ProductVO updateProduct(Long productId, ProductUpdateDTO updateDTO, Long userId);

    /**
     * 转换为VO
     */
    ProductVO convertToVO(Product product, Long currentUserId);

    /**
     * 获取附近商品列表
     *
     * @param page     分页参数
     * @param userId   当前用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius   半径（公里）
     * @return 商品列表
     */
    IPage<ProductVO> getNearbyProducts(Page<Product> page, Long userId,
                                       BigDecimal latitude, BigDecimal longitude, Integer radius);

    /**
     * 审核商品
     *
     * @param auditDTO  审核信息
     * @param auditorId 审核人ID
     * @return 审核后的商品信息
     */
    ProductAuditVO auditProduct(ProductAuditDTO auditDTO, Long auditorId);

    /**
     * 获取待审核商品列表
     *
     * @param page 分页参数
     * @return 待审核商品列表
     */
    IPage<ProductAuditVO> getPendingProducts(Page<Product> page);

    /**
     * 获取所有商品审核列表（管理员）
     *
     * @param page       分页参数
     * @param auditStatus 审核状态筛选
     * @return 商品审核列表
     */
    IPage<ProductAuditVO> getAllProductAudits(Page<Product> page, Integer auditStatus);

    /**
     * 获取商品审核详情
     *
     * @param productId 商品ID
     * @return 商品审核详情
     */
    ProductAuditVO getProductAuditDetail(Long productId);
}
