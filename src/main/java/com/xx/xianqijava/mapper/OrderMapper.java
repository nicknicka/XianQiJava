package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 统计指定状态的订单总金额
     * @param status 订单状态
     * @return 订单总金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM `order` WHERE status = #{status}")
    BigDecimal sumAmountByStatus(@Param("status") Integer status);

    /**
     * 统计指定时间后的订单总金额
     * @param dateTime 时间
     * @param status 订单状态
     * @return 订单总金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM `order` WHERE create_time >= #{dateTime} AND status = #{status}")
    BigDecimal sumAmountAfter(@Param("dateTime") LocalDateTime dateTime, @Param("status") Integer status);
}
