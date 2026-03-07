package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.CreditDailyStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 信用分每日统计 Mapper
 */
@Mapper
public interface CreditDailyStatMapper extends BaseMapper<CreditDailyStat> {

    /**
     * 获取用户在指定日期的统计记录
     */
    @Select("SELECT * FROM credit_daily_stat " +
            "WHERE user_id = #{userId} " +
            "AND stat_date = #{statDate}")
    CreditDailyStat getByUserAndDate(@Param("userId") String userId, @Param("statDate") LocalDate statDate);

    /**
     * 获取用户最近N天的统计记录
     */
    @Select("SELECT * FROM credit_daily_stat " +
            "WHERE user_id = #{userId} " +
            "AND stat_date >= #{startDate} " +
            "ORDER BY stat_date DESC")
    List<CreditDailyStat> getRecentStats(@Param("userId") String userId, @Param("startDate") LocalDate startDate);

    /**
     * 统计用户在指定日期范围内的总加分
     */
    @Select("SELECT COALESCE(SUM(total_gain), 0) FROM credit_daily_stat " +
            "WHERE user_id = #{userId} " +
            "AND stat_date >= #{startDate} " +
            "AND stat_date <= #{endDate}")
    Double sumGainByDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
