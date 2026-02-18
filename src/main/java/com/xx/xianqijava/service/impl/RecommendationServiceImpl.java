package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.entity.ProductViewHistory;
import com.xx.xianqijava.service.ProductFavoriteService;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.service.ProductViewHistoryService;
import com.xx.xianqijava.service.RecommendationService;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductViewHistoryService productViewHistoryService;
    private final ProductFavoriteService productFavoriteService;
    private final ProductService productService;

    @Override
    public List<ProductVO> getRecommendationsByHistory(Long userId, Integer limit) {
        log.info("基于浏览历史推荐, userId={}, limit={}", userId, limit);

        // 1. 获取用户浏览历史
        LambdaQueryWrapper<ProductViewHistory> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(ProductViewHistory::getUserId, userId)
                .orderByDesc(ProductViewHistory::getViewTime)
                .last("LIMIT 50"); // 只考虑最近50条浏览记录

        List<ProductViewHistory> historyList = productViewHistoryService.list(historyWrapper);
        if (historyList.isEmpty()) {
            return getHotProducts(null, limit);
        }

        // 2. 提取浏览过的商品分类
        Set<Long> viewedCategoryIds = new HashSet<>();
        Set<Long> viewedProductIds = new HashSet<>();
        for (ProductViewHistory history : historyList) {
            viewedProductIds.add(history.getProductId());
            Product product = productService.getById(history.getProductId());
            if (product != null && product.getCategoryId() != null) {
                viewedCategoryIds.add(product.getCategoryId());
            }
        }

        // 3. 查询同类别的热门商品（排除已浏览的）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getCategoryId, viewedCategoryIds)
                .eq(Product::getStatus, 1) // 在售
                .notIn(Product::getProductId, viewedProductIds) // 排除已浏览
                .orderByDesc(Product::getViewCount) // 按浏览量排序
                .last("LIMIT " + limit);

        List<Product> products = productService.list(wrapper);
        return products.stream()
                .map(product -> productService.convertToVO(product, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getRecommendationsByFavorites(Long userId, Integer limit) {
        log.info("基于收藏推荐, userId={}, limit={}", userId, limit);

        // 1. 获取用户收藏的商品
        LambdaQueryWrapper<ProductFavorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(ProductFavorite::getUserId, userId)
                .orderByDesc(ProductFavorite::getCreateTime);

        List<ProductFavorite> favoriteList = productFavoriteService.list(favoriteWrapper);
        if (favoriteList.isEmpty()) {
            return getHotProducts(null, limit);
        }

        // 2. 提取收藏商品的分类
        Set<Long> favoriteCategoryIds = new HashSet<>();
        Set<Long> favoriteProductIds = new HashSet<>();
        for (ProductFavorite favorite : favoriteList) {
            favoriteProductIds.add(favorite.getProductId());
            Product product = productService.getById(favorite.getProductId());
            if (product != null && product.getCategoryId() != null) {
                favoriteCategoryIds.add(product.getCategoryId());
            }
        }

        // 3. 查询同类别的热门商品（排除已收藏的）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getCategoryId, favoriteCategoryIds)
                .eq(Product::getStatus, 1)
                .notIn(Product::getProductId, favoriteProductIds)
                .orderByDesc(Product::getFavoriteCount) // 按收藏量排序
                .last("LIMIT " + limit);

        List<Product> products = productService.list(wrapper);
        return products.stream()
                .map(product -> productService.convertToVO(product, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getRecommendationsByCollaborative(Long userId, Integer limit) {
        log.info("基于协同过滤推荐, userId={}, limit={}", userId, limit);

        // 1. 找出浏览过相似商品的用户
        LambdaQueryWrapper<ProductViewHistory> myHistoryWrapper = new LambdaQueryWrapper<>();
        myHistoryWrapper.eq(ProductViewHistory::getUserId, userId)
                .orderByDesc(ProductViewHistory::getViewTime)
                .last("LIMIT 20");

        List<ProductViewHistory> myHistory = productViewHistoryService.list(myHistoryWrapper);
        if (myHistory.isEmpty()) {
            return getHotProducts(null, limit);
        }

        // 2. 获取我浏览过的商品ID
        Set<Long> myViewedProductIds = myHistory.stream()
                .map(ProductViewHistory::getProductId)
                .collect(Collectors.toSet());

        // 3. 找出也浏览过这些商品的其他用户
        LambdaQueryWrapper<ProductViewHistory> otherUsersWrapper = new LambdaQueryWrapper<>();
        otherUsersWrapper.in(ProductViewHistory::getProductId, myViewedProductIds)
                .ne(ProductViewHistory::getUserId, userId);

        List<ProductViewHistory> otherUsersHistory = productViewHistoryService.list(otherUsersWrapper);

        // 4. 获取这些用户浏览过的、但我没浏览过的商品
        Set<Long> otherUsersViewedProductIds = otherUsersHistory.stream()
                .map(ProductViewHistory::getProductId)
                .collect(Collectors.toSet());

        Set<Long> recommendedProductIds = new HashSet<>(otherUsersViewedProductIds);
        recommendedProductIds.removeAll(myViewedProductIds); // 排除我已浏览的

        if (recommendedProductIds.isEmpty()) {
            return getHotProducts(null, limit);
        }

        // 5. 按照商品热度排序
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getProductId, recommendedProductIds)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getViewCount, Product::getFavoriteCount)
                .last("LIMIT " + limit);

        List<Product> products = productService.list(wrapper);
        return products.stream()
                .map(product -> productService.convertToVO(product, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getPersonalizedRecommendations(Long userId, Integer limit) {
        log.info("综合推荐, userId={}, limit={}", userId, limit);

        // 综合多种推荐算法，权重分配：
        // - 浏览历史推荐：40%
        // - 收藏推荐：30%
        // - 协同过滤推荐：20%
        // - 热门商品：10%

        List<ProductVO> recommendations = new ArrayList<>();
        Set<Long> recommendedIds = new HashSet<>();

        // 1. 基于浏览历史推荐（40%）
        int historyCount = (int) Math.ceil(limit * 0.4);
        List<ProductVO> historyRec = getRecommendationsByHistory(userId, historyCount);
        recommendations.addAll(historyRec);
        historyRec.forEach(vo -> recommendedIds.add(vo.getProductId()));

        // 2. 基于收藏推荐（30%）
        int favoriteCount = (int) Math.ceil(limit * 0.3);
        List<ProductVO> favoriteRec = getRecommendationsByFavorites(userId, favoriteCount);
        for (ProductVO vo : favoriteRec) {
            if (!recommendedIds.contains(vo.getProductId())) {
                recommendations.add(vo);
                recommendedIds.add(vo.getProductId());
            }
        }

        // 3. 协同过滤推荐（20%）
        int collabCount = (int) Math.ceil(limit * 0.2);
        List<ProductVO> collabRec = getRecommendationsByCollaborative(userId, collabCount);
        for (ProductVO vo : collabRec) {
            if (!recommendedIds.contains(vo.getProductId())) {
                recommendations.add(vo);
                recommendedIds.add(vo.getProductId());
            }
        }

        // 4. 如果还不够，补充热门商品
        if (recommendations.size() < limit) {
            int remainCount = limit - recommendations.size();
            List<ProductVO> hotProducts = getHotProducts(null, remainCount + 5);
            for (ProductVO vo : hotProducts) {
                if (!recommendedIds.contains(vo.getProductId())) {
                    recommendations.add(vo);
                    recommendedIds.add(vo.getProductId());
                    if (recommendations.size() >= limit) {
                        break;
                    }
                }
            }
        }

        return recommendations.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getHotProducts(Long categoryId, Integer limit) {
        log.info("热门商品推荐, categoryId={}, limit={}", categoryId, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1) // 在售
                .orderByDesc(Product::getViewCount) // 按浏览量排序
                .orderByDesc(Product::getFavoriteCount); // 再按收藏量排序

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        wrapper.last("LIMIT " + limit);

        List<Product> products = productService.list(wrapper);
        return products.stream()
                .map(product -> productService.convertToVO(product, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getNewProducts(Integer limit) {
        log.info("新品推荐, limit={}", limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1) // 在售
                .orderByDesc(Product::getCreateTime) // 按创建时间排序
                .last("LIMIT " + limit);

        List<Product> products = productService.list(wrapper);
        return products.stream()
                .map(product -> productService.convertToVO(product, null))
                .collect(Collectors.toList());
    }
}
