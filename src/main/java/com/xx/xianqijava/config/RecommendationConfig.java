package com.xx.xianqijava.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 推荐系统配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationConfig {

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 缓存过期时间（秒）
     */
    private long cacheExpireSeconds = 300;

    /**
     * 推荐权重配置
     */
    private WeightConfig weight = new WeightConfig();

    /**
     * 地理位置推荐配置
     */
    private GeoConfig geo = new GeoConfig();

    /**
     * 多样性配置
     */
    private DiversityConfig diversity = new DiversityConfig();

    @Data
    public static class WeightConfig {
        /**
         * 浏览历史推荐权重 (40%)
         */
        private double historyWeight = 0.4;

        /**
         * 收藏推荐权重 (30%)
         */
        private double favoriteWeight = 0.3;

        /**
         * 协同过滤推荐权重 (20%)
         */
        private double collaborativeWeight = 0.2;

        /**
         * 热门商品权重 (10%)
         */
        private double hotWeight = 0.1;

        /**
         * 地理位置推荐权重
         */
        private double geoWeight = 0.15;
    }

    @Data
    public static class GeoConfig {
        /**
         * 是否启用地理位置推荐
         */
        private boolean enabled = true;

        /**
         * 附近商品距离范围（公里）
         */
        private double nearbyRadiusKm = 3.0;

        /**
         * 地理位置评分衰减因子（距离越远评分越低）
         */
        private double distanceDecayFactor = 0.5;
    }

    @Data
    public static class DiversityConfig {
        /**
         * 是否启用多样性推荐
         */
        private boolean enabled = true;

        /**
         * 同一分类商品最大占比 (0-1)
         */
        private double maxCategoryRatio = 0.6;

        /**
         * 新鲜度因子（新品加权）
         */
        private double freshnessFactor = 0.2;
    }
}
