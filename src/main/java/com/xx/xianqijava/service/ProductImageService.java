package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.ProductImageCreateDTO;
import com.xx.xianqijava.entity.ProductImage;
import com.xx.xianqijava.vo.ProductImageVO;

import java.util.List;

/**
 * 商品图片服务接口
 */
public interface ProductImageService extends IService<ProductImage> {

    /**
     * 添加商品图片
     *
     * @param productId       商品ID
     * @param imageCreateDTO 图片信息
     * @param userId          当前用户ID
     * @return 图片VO
     */
    ProductImageVO addProductImage(Long productId, ProductImageCreateDTO imageCreateDTO, Long userId);

    /**
     * 删除商品图片
     *
     * @param imageId 图片ID
     * @param userId  当前用户ID
     */
    void deleteProductImage(Long imageId, Long userId);

    /**
     * 设置封面图
     *
     * @param imageId 图片ID
     * @param userId  当前用户ID
     */
    void setCoverImage(Long imageId, Long userId);

    /**
     * 获取商品图片列表
     *
     * @param productId 商品ID
     * @return 图片列表
     */
    List<ProductImageVO> getProductImages(Long productId);
}
