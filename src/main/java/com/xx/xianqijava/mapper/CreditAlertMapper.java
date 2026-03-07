package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.CreditAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 信用分预警 Mapper
 */
@Mapper
public interface CreditAlertMapper extends BaseMapper<CreditAlert> {

    /**
     * 获取待处理的预警列表
     */
    @Select("SELECT * FROM credit_alert " +
            "WHERE status = #{status} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}")
    List<CreditAlert> getPendingAlerts(@Param("status") String status, @Param("limit") int limit);

    /**
     * 获取用户的预警列表
     */
    @Select("SELECT * FROM credit_alert " +
            "WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}")
    List<CreditAlert> getUserAlerts(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 检查用户是否有未处理的指定类型预警
     */
    @Select("SELECT COUNT(*) FROM credit_alert " +
            "WHERE user_id = #{userId} " +
            "AND alert_type = #{alertType} " +
            "AND status IN ('pending', 'reviewing') " +
            "AND created_at >= #{since}")
    int countPendingAlertsByType(
        @Param("userId") String userId,
        @Param("alertType") String alertType,
        @Param("since") LocalDateTime since
    );
}
