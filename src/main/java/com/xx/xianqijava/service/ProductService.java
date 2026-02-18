package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.ProductCreateDTO;
import com.xx.xianqijava.dto.ProductUpdateDTO;
import com.xx.xianqijava.entity.Product;
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
}
