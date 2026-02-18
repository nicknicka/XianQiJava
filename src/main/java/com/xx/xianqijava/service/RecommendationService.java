package com.xx.xianqijava.service;

import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.vo.ProductVO;

import java.util.List;

/**
 * 推荐服务接口
 */
public interface RecommendationService {

    /**
     * 基于浏览历史的推荐商品
     *
     * @param userId 用户ID
     * @param limit  推荐数量
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendationsByHistory(Long userId, Integer limit);

    /**
     * 基于收藏的推荐商品
     *
     * @param userId 用户ID
     * @param limit  推荐数量
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendationsByFavorites(Long userId, Integer limit);

    /**
     * 基于协同过滤的推荐商品（浏览过相似商品的用户也浏览过的商品）
     *
     * @param userId 用户ID
     * @param limit  推荐数量
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendationsByCollaborative(Long userId, Integer limit);

    /**
     * 综合推荐（结合多种推荐算法）
     *
     * @param userId 用户ID
     * @param limit  推荐数量
     * @return 推荐商品列表
     */
    List<ProductVO> getPersonalizedRecommendations(Long userId, Integer limit);

    /**
     * 热门商品推荐
     *
     * @param categoryId 分类ID（可选）
     * @param limit     推荐数量
     * @return 推荐商品列表
     */
    List<ProductVO> getHotProducts(Long categoryId, Integer limit);

    /**
     * 新品推荐
     *
     * @param limit 推荐数量
     * @return 推荐商品列表
     */
    List<ProductVO> getNewProducts(Integer limit);
}
