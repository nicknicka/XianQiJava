package com.xx.xianqijava.service;

import com.xx.xianqijava.mapper.UserCreditExtMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户活跃时间服务
 */
@Slf4j
@Service
public class UserActiveService {

    @Autowired
    private UserCreditExtMapper userCreditExtMapper;

    /**
     * 更新用户最后活跃时间
     * 在用户登录、进行重要操作时调用
     */
    public void updateLastActiveTime(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            int updated = userCreditExtMapper.updateLastActiveTime(userId, LocalDateTime.now());
            if (updated > 0) {
                log.debug("用户 {} 活跃时间已更新", userId);
            }
        } catch (Exception e) {
            log.error("更新用户 {} 活跃时间失败", userId, e);
        }
    }

    /**
     * 更新用户最后活跃时间（String类型userId）
     */
    public void updateLastActiveTime(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            updateLastActiveTime(userIdLong);
        } catch (NumberFormatException e) {
            log.error("无效的用户ID格式: {}", userId, e);
        }
    }
}
