package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.SystemNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统通知Mapper
 */
@Mapper
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

    /**
     * 查询用户的未读通知数量
     */
    int countUnreadNotifications(@Param("userId") Long userId);

    /**
     * 批量插入阅读记录
     */
    int batchInsertReadRecords(@Param("userId") Long userId);
}
