package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.FlashSaleProductRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 秒杀商品关联 Mapper
 */
@Mapper
public interface FlashSaleProductRelationMapper extends BaseMapper<FlashSaleProductRelation> {

    /**
     * 查询场次下的商品
     */
    @Select("SELECT * FROM flash_sale_product_relation " +
            "WHERE session_id = #{sessionId} " +
            "ORDER BY sort_order ASC")
    List<FlashSaleProductRelation> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询活动下的所有商品
     */
    @Select("SELECT * FROM flash_sale_product_relation " +
            "WHERE activity_id = #{activityId} " +
            "ORDER BY sort_order ASC")
    List<FlashSaleProductRelation> selectByActivityId(@Param("activityId") Long activityId);
}
