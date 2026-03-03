package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.FlashSaleOrderExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 秒杀订单扩展 Mapper
 */
@Mapper
public interface FlashSaleOrderExtMapper extends BaseMapper<FlashSaleOrderExt> {

    /**
     * 查询用户在某活动中的购买数量
     */
    @Select("SELECT COUNT(*) FROM flash_sale_order_ext " +
            "WHERE user_id = #{userId} " +
            "AND activity_id = #{activityId}")
    int countByUserAndActivity(@Param("userId") Long userId,
                               @Param("activityId") Long activityId);

    /**
     * 查询用户在某场次中的购买数量
     */
    @Select("SELECT COUNT(*) FROM flash_sale_order_ext " +
            "WHERE user_id = #{userId} " +
            "AND session_id = #{sessionId}")
    int countByUserAndSession(@Param("userId") Long userId,
                              @Param("sessionId") Long sessionId);

    /**
     * 查询用户在某商品的秒杀订单
     */
    @Select("SELECT * FROM flash_sale_order_ext " +
            "WHERE user_id = #{userId} " +
            "AND product_id = #{productId} " +
            "ORDER BY create_time DESC")
    List<FlashSaleOrderExt> selectByUserAndProduct(@Param("userId") Long userId,
                                                 @Param("productId") Long productId);
}
