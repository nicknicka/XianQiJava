package com.xx.xianqijava.service;

import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.vo.ProductVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐系统辅助服务
 * 提供评分计算、多样性处理、地理位置计算等功能
 */
@Service
public class RecommendationHelperService {

    /**
     * 计算商品的综合推荐评分
     *
     * @param product           商品
     * @param favoriteCount     收藏数量（从 ProductVO 获取或实时查询）
     * @param viewCountWeight   浏览量权重
     * @param favoriteCountWeight 收藏量权重
     * @param freshnessWeight   新鲜度权重
     * @param daysSinceCreated  商品发布天数
     * @return 综合评分 (0-100)
     */
    public double calculateProductScore(Product product,
                                        int favoriteCount,
                                        double viewCountWeight,
                                        double favoriteCountWeight,
                                        double freshnessWeight,
                                        long daysSinceCreated) {
        // 归一化浏览量评分 (0-30分)
        double viewScore = Math.min(30, Math.log10(product.getViewCount() + 1) * 10);

        // 归一化收藏量评分 (0-30分)
        double favoriteScore = Math.min(30, Math.log10(favoriteCount + 1) * 10);

        // 新鲜度评分 (0-40分，越新分数越高)
        double freshnessScore = Math.max(0, 40 - daysSinceCreated * freshnessWeight);

        return viewScore * viewCountWeight
                + favoriteScore * favoriteCountWeight
                + freshnessScore * freshnessWeight;
    }

    /**
     * 计算地理位置衰减评分
     *
     * @param distanceKm 距离（公里）
     * @param decayFactor 衰减因子
     * @return 距离评分 (0-1)
     */
    public double calculateDistanceScore(double distanceKm, double decayFactor) {
        return Math.exp(-decayFactor * distanceKm);
    }

    /**
     * 使用 Haversine 公式计算两点之间的距离（公里）
     *
     * @param lat1 第一点纬度
     * @param lon1 第一点经度
     * @param lat2 第二点纬度
     * @param lon2 第二点经度
     * @return 距离（公里）
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) {
            return Double.MAX_VALUE;
        }

        final int EARTH_RADIUS = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 计算商品发布以来的天数
     *
     * @param createTime 创建时间
     * @return 天数
     */
    public long calculateDaysSinceCreated(LocalDateTime createTime) {
        if (createTime == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(createTime, LocalDateTime.now());
    }

    /**
     * 应用多样性过滤，避免推荐结果过于集中在某一分类
     *
     * @param products        原始商品列表
     * @param maxCategoryRatio 同一分类最大占比
     * @param limit           返回数量
     * @return 多样化后的商品列表
     */
    public List<ProductVO> applyDiversityFilter(List<ProductVO> products,
                                                double maxCategoryRatio,
                                                int limit) {
        if (products.isEmpty()) {
            return products;
        }

        List<ProductVO> result = new ArrayList<>();
        Map<Long, Integer> categoryCount = new HashMap<>();
        int maxPerCategory = (int) Math.ceil(limit * maxCategoryRatio);

        for (ProductVO product : products) {
            Long categoryId = product.getCategoryId() != null ? product.getCategoryId().longValue() : null;
            int currentCount = categoryCount.getOrDefault(categoryId, 0);

            if (currentCount < maxPerCategory) {
                result.add(product);
                categoryCount.put(categoryId, currentCount + 1);

                if (result.size() >= limit) {
                    break;
                }
            }
        }

        // 如果过滤后数量不足，从剩余商品中补充
        if (result.size() < limit) {
            Set<Long> addedIds = result.stream()
                    .map(ProductVO::getProductId)
                    .collect(Collectors.toSet());

            for (ProductVO product : products) {
                if (!addedIds.contains(product.getProductId())) {
                    result.add(product);
                    if (result.size() >= limit) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * 按评分排序商品
     *
     * @param productsWithScore 商品和评分的映射
     * @return 排序后的商品列表
     */
    public List<ProductVO> sortByScore(Map<ProductVO, Double> productsWithScore) {
        return productsWithScore.entrySet().stream()
                .sorted(Map.Entry.<ProductVO, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 打乱列表顺序（用于增加推荐随机性）
     *
     * @param list 原始列表
     * @return 打乱后的列表
     */
    public <T> List<T> shuffle(List<T> list) {
        List<T> result = new ArrayList<>(list);
        Collections.shuffle(result);
        return result;
    }

    /**
     * 生成推荐缓存键
     *
     * @param userId 用户ID
     * @param type   推荐类型
     * @param params 额外参数
     * @return 缓存键
     */
    public String buildCacheKey(Long userId, String type, Object... params) {
        StringBuilder key = new StringBuilder("recommend:")
                .append(type)
                .append(":")
                .append(userId);

        for (Object param : params) {
            key.append(":").append(param);
        }

        return key.toString();
    }

    /**
     * 计算两个用户之间的相似度（基于共同浏览的商品数量）
     *
     * @param user1ViewedProducts 用户1浏览的商品ID集合
     * @param user2ViewedProducts 用户2浏览的商品ID集合
     * @return 相似度评分 (0-1)
     */
    public double calculateUserSimilarity(Set<Long> user1ViewedProducts,
                                          Set<Long> user2ViewedProducts) {
        if (user1ViewedProducts.isEmpty() || user2ViewedProducts.isEmpty()) {
            return 0.0;
        }

        // 计算交集
        Set<Long> intersection = new HashSet<>(user1ViewedProducts);
        intersection.retainAll(user2ViewedProducts);

        // 计算并集
        Set<Long> union = new HashSet<>(user1ViewedProducts);
        union.addAll(user2ViewedProducts);

        // Jaccard 相似度
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}
