package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.vo.ProductVO;

/**
 * 商品收藏服务接口
 */
public interface ProductFavoriteService extends IService<ProductFavorite> {

    /**
     * 添加收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void addFavorite(Long userId, Long productId);

    /**
     * 取消收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void removeFavorite(Long userId, Long productId);

    /**
     * 检查是否已收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long productId);

    /**
     * 获取用户的收藏列表（分页）
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 收藏的商品列表
     */
    IPage<ProductVO> getFavoriteList(Long userId, Page<ProductFavorite> page);
}
