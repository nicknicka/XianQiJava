package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.FlashSaleProductExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀商品扩展 Mapper
 */
@Mapper
public interface FlashSaleProductExtMapper extends BaseMapper<FlashSaleProductExt> {

    /**
     * 查询当前进行中的秒杀商品
     */
    @Select("SELECT * FROM flash_sale_product_ext " +
            "WHERE status = 1 " +
            "AND start_time <= #{now} " +
            "AND end_time > #{now} " +
            "ORDER BY sort_order DESC")
    List<FlashSaleProductExt> selectCurrentFlashProducts(@Param("now") LocalDateTime now);

    /**
     * 根据商品ID查询秒杀配置
     */
    @Select("SELECT * FROM flash_sale_product_ext WHERE product_id = #{productId}")
    FlashSaleProductExt selectByProductId(@Param("productId") Long productId);
}
