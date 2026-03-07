package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 信用分记录 Mapper
 */
@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {

    /**
     * 统计用户在指定日期的总加分
     */
    @Select("SELECT COALESCE(SUM(score_change), 0) FROM credit_record " +
            "WHERE user_id = #{userId} " +
            "AND DATE(created_at) = #{date} " +
            "AND score_change > 0")
    BigDecimal sumGainByDate(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 统计用户在指定日期范围内的总加分
     */
    @Select("SELECT COALESCE(SUM(score_change), 0) FROM credit_record " +
            "WHERE user_id = #{userId} " +
            "AND created_at >= #{startDate} " +
            "AND created_at < #{endDate} " +
            "AND score_change > 0")
    BigDecimal sumGainByDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 获取用户最近的信用分记录
     */
    @Select("SELECT * FROM credit_record " +
            "WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}")
    List<CreditRecord> getRecentRecords(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 统计用户在指定时间范围内的信用分变化
     */
    @Select("SELECT COALESCE(SUM(score_change), 0) FROM credit_record " +
            "WHERE user_id = #{userId} " +
            "AND created_at >= #{startDate} " +
            "AND created_at < #{endDate}")
    BigDecimal sumChangeByDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
