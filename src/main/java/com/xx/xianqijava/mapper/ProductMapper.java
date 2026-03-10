package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询商品列表
     */
    IPage<Product> selectProductPage(Page<Product> page, @Param("categoryId") Integer categoryId,
                                     @Param("status") Integer status, @Param("keyword") String keyword,
                                     @Param("sortBy") String sortBy, @Param("priceAsc") Boolean priceAsc);

    /**
     * 查询商品的秒杀信息
     */
    Map<String, Object> selectFlashSaleInfo(@Param("productId") Long productId, @Param("now") LocalDateTime now);

    /**
     * 增加商品浏览量
     */
    void incrementViewCount(@Param("productId") Long productId);

    // 注意：收藏计数不再维护缓存字段，已移除 incrementFavoriteCount 和 decrementFavoriteCount
    // 收藏数通过实时查询 product_favorite 表统计
}
