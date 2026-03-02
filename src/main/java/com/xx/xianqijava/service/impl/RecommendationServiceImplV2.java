package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.config.RecommendationConfig;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.entity.ProductViewHistory;
import com.xx.xianqijava.service.*;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类 V2 - 优化版本
 * 特性：Redis缓存、地理位置推荐、多样性控制、推荐评分
 */
@Slf4j
@Service("recommendationServiceV2")
@RequiredArgsConstructor
public class RecommendationServiceImplV2 {

    private final ProductViewHistoryService productViewHistoryService;
    private final ProductFavoriteService productFavoriteService;
    private final ProductService productService;
    private final RecommendationHelperService helperService;
    private final RecommendationConfig config;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "recommend:";

    // ==================== 核心推荐方法 ====================

    /**
     * 综合推荐（结合多种推荐算法）- 优化版本
     * 支持缓存、地理位置、多样性控制
     */
    public List<ProductVO> getPersonalizedRecommendations(Long userId, Integer limit,
                                                           BigDecimal userLatitude,
                                                           BigDecimal userLongitude) {
        log.info("综合推荐V2, userId={}, limit={}, location=({}, {})",
                userId, limit, userLatitude, userLongitude);

        // 1. 尝试从缓存获取
        String cacheKey = helperService.buildCacheKey(userId, "personalized", limit,
                userLatitude, userLongitude);
        List<ProductVO> cached = getFromCache(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.info("从缓存获取推荐结果, userId={}, size={}", userId, cached.size());
            return cached;
        }

        // 2. 获取各推荐结果并计算评分
        Map<ProductVO, Double> scoredProducts = new HashMap<>();
        RecommendationConfig.WeightConfig weight = config.getWeight();

        // 基于浏览历史推荐
        int historyCount = (int) Math.ceil(limit * weight.getHistoryWeight());
        addRecommendationsWithScore(userId, historyCount, "history", weight.getHistoryWeight(), scoredProducts);

        // 基于收藏推荐
        int favoriteCount = (int) Math.ceil(limit * weight.getFavoriteWeight());
        addRecommendationsWithScore(userId, favoriteCount, "favorite", weight.getFavoriteWeight(), scoredProducts);

        // 协同过滤推荐
        int collabCount = (int) Math.ceil(limit * weight.getCollaborativeWeight());
        addCollaborativeRecommendationsWithScore(userId, collabCount, weight.getCollaborativeWeight(), scoredProducts);

        // 地理位置推荐
        if (config.getGeo().isEnabled() && userLatitude != null && userLongitude != null) {
            int geoCount = (int) Math.ceil(limit * weight.getGeoWeight());
            addGeoRecommendationsWithScore(userId, geoCount, userLatitude, userLongitude,
                    weight.getGeoWeight(), scoredProducts);
        }

        // 热门商品补充
        if (scoredProducts.size() < limit) {
            int hotCount = limit - scoredProducts.size() + 5;
            addHotProductsWithScore(null, hotCount, weight.getHotWeight(), scoredProducts);
        }

        // 3. 按评分排序
        List<ProductVO> recommendations = helperService.sortByScore(scoredProducts);

        // 4. 应用多样性过滤
        if (config.getDiversity().isEnabled()) {
            recommendations = helperService.applyDiversityFilter(
                    recommendations,
                    config.getDiversity().getMaxCategoryRatio(),
                    limit
            );
        } else {
            recommendations = recommendations.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // 5. 缓存结果
        saveToCache(cacheKey, recommendations);

        log.info("推荐完成, userId={}, resultSize={}", userId, recommendations.size());
        return recommendations;
    }

    /**
     * 基于浏览历史的推荐 - 优化版本
     */
    public List<ProductVO> getRecommendationsByHistory(Long userId, Integer limit) {
        log.info("基于浏览历史推荐V2, userId={}, limit={}", userId, limit);

        String cacheKey = helperService.buildCacheKey(userId, "history", limit);
        List<ProductVO> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 获取用户浏览历史
        List<ProductViewHistory> historyList = getUserViewHistory(userId, 50);
        if (historyList.isEmpty()) {
            return getHotProducts(null, limit);
        }

        // 提取浏览商品的分类
        Map<String, Set<Long>> data = extractCategoryAndProductIds(historyList);
        Set<Long> viewedCategoryIds = data.get("categories");
        Set<Long> viewedProductIds = data.get("products");

        // 查询同类别的热门商品，计算评分并排序
        Map<ProductVO, Double> scoredProducts = new HashMap<>();
        List<Product> products = getProductsByCategories(viewedCategoryIds, viewedProductIds, limit * 3);

        for (Product product : products) {
            ProductVO vo = productService.convertToVO(product, userId);
            double score = calculateProductScore(product, 0.4, 0.4, 0.2);
            scoredProducts.put(vo, score);
        }

        List<ProductVO> result = helperService.sortByScore(scoredProducts).stream()
                .limit(limit)
                .collect(Collectors.toList());

        saveToCache(cacheKey, result);
        return result;
    }

    /**
     * 基于收藏的推荐 - 优化版本
     */
    public List<ProductVO> getRecommendationsByFavorites(Long userId, Integer limit) {
        log.info("基于收藏推荐V2, userId={}, limit={}", userId, limit);

        String cacheKey = helperService.buildCacheKey(userId, "favorite", limit);
        List<ProductVO> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 获取用户收藏
        List<ProductFavorite> favoriteList = getUserFavorites(userId);
        if (favoriteList.isEmpty()) {
            return getHotProducts(null, limit);
        }

        // 提取收藏商品的分类
        Map<String, Set<Long>> data = extractCategoryAndProductIdsFromFavorites(favoriteList);
        Set<Long> favoriteCategoryIds = data.get("categories");
        Set<Long> favoriteProductIds = data.get("products");

        // 查询同类别的热门商品
        Map<ProductVO, Double> scoredProducts = new HashMap<>();
        List<Product> products = getProductsByCategories(favoriteCategoryIds, favoriteProductIds, limit * 3);

        for (Product product : products) {
            ProductVO vo = productService.convertToVO(product, userId);
            double score = calculateProductScore(product, 0.3, 0.5, 0.2);
            scoredProducts.put(vo, score);
        }

        List<ProductVO> result = helperService.sortByScore(scoredProducts).stream()
                .limit(limit)
                .collect(Collectors.toList());

        saveToCache(cacheKey, result);
        return result;
    }

    /**
     * 协同过滤推荐 - 优化版本（带用户相似度计算）
     */
    public List<ProductVO> getRecommendationsByCollaborative(Long userId, Integer limit) {
        log.info("基于协同过滤推荐V2, userId={}, limit={}", userId, limit);

        String cacheKey = helperService.buildCacheKey(userId, "collaborative", limit);
        List<ProductVO> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 获取用户浏览历史
        List<ProductViewHistory> myHistory = getUserViewHistory(userId, 20);
        if (myHistory.isEmpty()) {
            return getHotProducts(null, limit);
        }

        Set<Long> myViewedProductIds = myHistory.stream()
                .map(ProductViewHistory::getProductId)
                .collect(Collectors.toSet());

        // 找出相似用户
        Map<Long, Double> similarUsers = findSimilarUsers(userId, myViewedProductIds, 20);

        if (similarUsers.isEmpty()) {
            return getHotProducts(null, limit);
        }

        // 获取相似用户浏览过但当前用户未浏览过的商品
        Map<ProductVO, Double> scoredProducts = new HashMap<>();
        Set<Long> recommendedIds = new HashSet<>();

        for (Map.Entry<Long, Double> entry : similarUsers.entrySet()) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();

            List<ProductViewHistory> otherHistory = getUserViewHistory(similarUserId, null);
            for (ProductViewHistory history : otherHistory) {
                Long productId = history.getProductId();
                if (!myViewedProductIds.contains(productId) && !recommendedIds.contains(productId)) {
                    Product product = productService.getById(productId);
                    if (product != null && product.getStatus() == 1) {
                        ProductVO vo = productService.convertToVO(product, userId);
                        // 综合相似度和商品热度计算评分
                        double score = similarity * 50 + calculateProductScore(product, 0.3, 0.3, 0.4);
                        scoredProducts.put(vo, score);
                        recommendedIds.add(productId);
                    }
                }
            }
        }

        List<ProductVO> result = helperService.sortByScore(scoredProducts).stream()
                .limit(limit)
                .collect(Collectors.toList());

        saveToCache(cacheKey, result);
        return result;
    }

    /**
     * 基于地理位置的推荐
     */
    public List<ProductVO> getRecommendationsByLocation(Long userId, Integer limit,
                                                        BigDecimal userLatitude,
                                                        BigDecimal userLongitude) {
        log.info("基于地理位置推荐, userId={}, limit={}, location=({}, {})",
                userId, limit, userLatitude, userLongitude);

        if (userLatitude == null || userLongitude == null) {
            return getHotProducts(null, limit);
        }

        String cacheKey = helperService.buildCacheKey(userId, "geo", limit, userLatitude, userLongitude);
        List<ProductVO> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 获取所有在售商品
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .isNotNull(Product::getLatitude)
                .isNotNull(Product::getLongitude);

        List<Product> allProducts = productService.list(wrapper);

        // 计算距离并评分
        Map<ProductVO, Double> scoredProducts = new HashMap<>();
        for (Product product : allProducts) {
            double distance = helperService.calculateDistance(
                    userLatitude.doubleValue(),
                    userLongitude.doubleValue(),
                    product.getLatitude().doubleValue(),
                    product.getLongitude().doubleValue()
            );

            // 只推荐附近的商品
            if (distance <= config.getGeo().getNearbyRadiusKm()) {
                ProductVO vo = productService.convertToVO(product, userId);
                // 距离评分 + 商品热度评分
                double distanceScore = helperService.calculateDistanceScore(
                        distance, config.getGeo().getDistanceDecayFactor());
                double productScore = calculateProductScore(product, 0.3, 0.3, 0.4);
                double totalScore = distanceScore * 50 + productScore;
                scoredProducts.put(vo, totalScore);
            }
        }

        List<ProductVO> result = helperService.sortByScore(scoredProducts).stream()
                .limit(limit)
                .collect(Collectors.toList());

        saveToCache(cacheKey, result);
        return result;
    }

    /**
     * 热门商品推荐 - 优化版本
     */
    public List<ProductVO> getHotProducts(Long categoryId, Integer limit) {
        log.info("热门商品推荐V2, categoryId={}, limit={}", categoryId, limit);

        String cacheKey = helperService.buildCacheKey(0L, "hot", categoryId, limit);
        List<ProductVO> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 按浏览量和收藏量综合排序
        wrapper.orderByDesc(Product::getViewCount)
                .orderByDesc(Product::getFavoriteCount)
                .last("LIMIT " + (limit * 2)); // 多取一些用于排序

        List<Product> products = productService.list(wrapper);

        // 计算评分并排序
        Map<ProductVO, Double> scoredProducts = new HashMap<>();
        for (Product product : products) {
            ProductVO vo = productService.convertToVO(product, null);
            double score = calculateProductScore(product, 0.5, 0.5, 0.0);
            scoredProducts.put(vo, score);
        }

        List<ProductVO> result = helperService.sortByScore(scoredProducts).stream()
                .limit(limit)
                .collect(Collectors.toList());

        saveToCache(cacheKey, result);
        return result;
    }

    /**
     * 新品推荐 - 优化版本
     */
    public List<ProductVO> getNewProducts(Integer limit) {
        log.info("新品推荐V2, limit={}", limit);

        String cacheKey = helperService.buildCacheKey(0L, "new", limit);
        List<ProductVO> cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit);

        List<Product> products = productService.list(wrapper);
        List<ProductVO> result = products.stream()
                .map(product -> productService.convertToVO(product, null))
                .collect(Collectors.toList());

        saveToCache(cacheKey, result);
        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算商品评分
     */
    private double calculateProductScore(Product product, double viewWeight,
                                         double favoriteWeight, double freshnessWeight) {
        long daysSinceCreated = helperService.calculateDaysSinceCreated(product.getCreateTime());
        return helperService.calculateProductScore(
                product, viewWeight, favoriteWeight, freshnessWeight, daysSinceCreated);
    }

    /**
     * 获取用户浏览历史
     */
    private List<ProductViewHistory> getUserViewHistory(Long userId, Integer limit) {
        LambdaQueryWrapper<ProductViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductViewHistory::getUserId, userId)
                .orderByDesc(ProductViewHistory::getViewTime);
        if (limit != null) {
            wrapper.last("LIMIT " + limit);
        }
        return productViewHistoryService.list(wrapper);
    }

    /**
     * 获取用户收藏列表
     */
    private List<ProductFavorite> getUserFavorites(Long userId) {
        LambdaQueryWrapper<ProductFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductFavorite::getUserId, userId)
                .orderByDesc(ProductFavorite::getCreateTime);
        return productFavoriteService.list(wrapper);
    }

    /**
     * 从浏览历史中提取分类和商品ID
     */
    private Map<String, Set<Long>> extractCategoryAndProductIds(List<ProductViewHistory> historyList) {
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();

        for (ProductViewHistory history : historyList) {
            productIds.add(history.getProductId());
            Product product = productService.getById(history.getProductId());
            if (product != null && product.getDeleted() == 0 && product.getCategoryId() != null) {
                categoryIds.add(product.getCategoryId());
            }
        }

        Map<String, Set<Long>> result = new HashMap<>();
        result.put("categories", categoryIds);
        result.put("products", productIds);
        return result;
    }

    /**
     * 从收藏列表中提取分类和商品ID
     */
    private Map<String, Set<Long>> extractCategoryAndProductIdsFromFavorites(List<ProductFavorite> favoriteList) {
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();

        for (ProductFavorite favorite : favoriteList) {
            productIds.add(favorite.getProductId());
            Product product = productService.getById(favorite.getProductId());
            if (product != null && product.getDeleted() == 0 && product.getCategoryId() != null) {
                categoryIds.add(product.getCategoryId());
            }
        }

        Map<String, Set<Long>> result = new HashMap<>();
        result.put("categories", categoryIds);
        result.put("products", productIds);
        return result;
    }

    /**
     * 根据分类获取商品列表
     */
    private List<Product> getProductsByCategories(Set<Long> categoryIds, Set<Long> excludeProductIds, int limit) {
        if (categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getCategoryId, categoryIds)
                .eq(Product::getStatus, 1)
                .notIn(Product::getProductId, excludeProductIds)
                .orderByDesc(Product::getViewCount)
                .last("LIMIT " + limit);

        return productService.list(wrapper);
    }

    /**
     * 找出相似用户
     */
    private Map<Long, Double> findSimilarUsers(Long userId, Set<Long> myViewedProductIds, int limit) {
        // 获取浏览过相同商品的其他用户
        LambdaQueryWrapper<ProductViewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProductViewHistory::getProductId, myViewedProductIds)
                .ne(ProductViewHistory::getUserId, userId);

        List<ProductViewHistory> otherUsersHistory = productViewHistoryService.list(wrapper);

        // 按用户分组
        Map<Long, Set<Long>> userViewedProducts = new HashMap<>();
        for (ProductViewHistory history : otherUsersHistory) {
            userViewedProducts
                    .computeIfAbsent(history.getUserId(), k -> new HashSet<>())
                    .add(history.getProductId());
        }

        // 计算相似度并排序
        Map<Long, Double> similarityMap = new HashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : userViewedProducts.entrySet()) {
            Long otherUserId = entry.getKey();
            Set<Long> otherViewedProducts = entry.getValue();
            double similarity = helperService.calculateUserSimilarity(myViewedProductIds, otherViewedProducts);
            if (similarity > 0.1) { // 只保留相似度大于0.1的用户
                similarityMap.put(otherUserId, similarity);
            }
        }

        // 返回相似度最高的用户
        return similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 添加带评分的推荐结果
     */
    private void addRecommendationsWithScore(Long userId, int count, String type,
                                             double baseWeight, Map<ProductVO, Double> scoredProducts) {
        List<ProductVO> recommendations;
        if ("history".equals(type)) {
            recommendations = getRecommendationsByHistory(userId, count);
        } else if ("favorite".equals(type)) {
            recommendations = getRecommendationsByFavorites(userId, count);
        } else {
            return;
        }

        for (ProductVO vo : recommendations) {
            double currentScore = scoredProducts.getOrDefault(vo, 0.0);
            scoredProducts.put(vo, currentScore + baseWeight * 100);
        }
    }

    /**
     * 添加协同过滤推荐（带评分）
     */
    private void addCollaborativeRecommendationsWithScore(Long userId, int count, double baseWeight,
                                                          Map<ProductVO, Double> scoredProducts) {
        List<ProductVO> recommendations = getRecommendationsByCollaborative(userId, count);
        for (ProductVO vo : recommendations) {
            double currentScore = scoredProducts.getOrDefault(vo, 0.0);
            scoredProducts.put(vo, currentScore + baseWeight * 100);
        }
    }

    /**
     * 添加地理位置推荐（带评分）
     */
    private void addGeoRecommendationsWithScore(Long userId, int count, BigDecimal lat, BigDecimal lon,
                                               double baseWeight, Map<ProductVO, Double> scoredProducts) {
        List<ProductVO> recommendations = getRecommendationsByLocation(userId, count, lat, lon);
        for (ProductVO vo : recommendations) {
            double currentScore = scoredProducts.getOrDefault(vo, 0.0);
            scoredProducts.put(vo, currentScore + baseWeight * 100);
        }
    }

    /**
     * 添加热门商品（带评分）
     */
    private void addHotProductsWithScore(Long categoryId, int count, double baseWeight,
                                        Map<ProductVO, Double> scoredProducts) {
        List<ProductVO> hotProducts = getHotProducts(categoryId, count);
        for (ProductVO vo : hotProducts) {
            if (!scoredProducts.containsKey(vo)) {
                scoredProducts.put(vo, baseWeight * 100);
            }
        }
    }

    // ==================== 缓存方法 ====================

    /**
     * 从缓存获取
     */
    @SuppressWarnings("unchecked")
    private List<ProductVO> getFromCache(String key) {
        if (!config.isCacheEnabled()) {
            return null;
        }
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_PREFIX + key);
            return cached != null ? (List<ProductVO>) cached : null;
        } catch (Exception e) {
            log.warn("从缓存获取失败, key={}", key, e);
            return null;
        }
    }

    /**
     * 保存到缓存
     */
    private void saveToCache(String key, List<ProductVO> value) {
        if (!config.isCacheEnabled() || value == null || value.isEmpty()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + key,
                    value,
                    config.getCacheExpireSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.warn("保存到缓存失败, key={}", key, e);
        }
    }
}
