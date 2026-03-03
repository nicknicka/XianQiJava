package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.FlashSaleSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀场次 Mapper
 */
@Mapper
public interface FlashSaleSessionMapper extends BaseMapper<FlashSaleSession> {

    /**
     * 查询活动下的所有场次
     */
    @Select("SELECT * FROM flash_sale_session " +
            "WHERE activity_id = #{activityId} " +
            "ORDER BY sort_order ASC")
    List<FlashSaleSession> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 查询当前可见的场次列表（包含即将开始和进行中的）
     */
    @Select("SELECT s.* FROM flash_sale_session s " +
            "INNER JOIN flash_sale_activity a ON s.activity_id = a.activity_id " +
            "WHERE a.deleted = 0 " +
            "AND s.start_time <= #{endTime} " +
            "AND s.end_time > #{startTime} " +
            "ORDER BY s.start_time ASC")
    List<FlashSaleSession> selectActiveSessions(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}
