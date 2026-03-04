package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.NotificationReadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知阅读记录 Mapper
 */
@Mapper
public interface NotificationReadRecordMapper extends BaseMapper<NotificationReadRecord> {

    /**
     * 批量插入阅读记录（使用 INSERT IGNORE 避免重复）
     */
    void batchInsertIgnore(@Param("records") List<NotificationReadRecord> records);

    /**
     * 检查用户是否已读通知
     */
    @Select("SELECT COUNT(*) FROM notification_read_records WHERE notification_id = #{notificationId} AND user_id = #{userId}")
    int countByNotificationIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * 获取用户已读的通知ID列表
     */
    @Select("SELECT DISTINCT notification_id FROM notification_read_records WHERE user_id = #{userId}")
    List<Long> findReadNotificationIdsByUserId(@Param("userId") Long userId);
}
