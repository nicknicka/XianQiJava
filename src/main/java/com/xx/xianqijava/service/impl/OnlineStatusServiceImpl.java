package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.service.OnlineStatusService;
import com.xx.xianqijava.service.WebSocketMessageService;
import com.xx.xianqijava.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线状态管理服务实现类
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineStatusServiceImpl implements OnlineStatusService {

    private final WebSocketHandler webSocketHandler;
    private final WebSocketMessageService webSocketMessageService;

    /**
     * 用户最后活跃时间缓存
     */
    private final Map<Long, Long> lastActiveTimeCache = new ConcurrentHashMap<>();

    @Override
    public void userOnline(Long userId) {
        if (userId == null) {
            return;
        }

        lastActiveTimeCache.put(userId, System.currentTimeMillis());
        log.debug("用户上线：userId={}", userId);

        // 可选：通知好友用户上线
        // notifyFriendsUserOnline(userId, true);
    }

    @Override
    public void userOffline(Long userId) {
        if (userId == null) {
            return;
        }

        lastActiveTimeCache.remove(userId);
        log.debug("用户下线：userId={}", userId);

        // 可选：通知好友用户下线
        // notifyFriendsUserOnline(userId, false);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }

        // 通过 WebSocket 检查实时在线状态
        return webSocketHandler.isUserOnline(userId);
    }

    @Override
    public Set<Long> getOnlineUserIds() {
        return webSocketHandler.getOnlineUserIds();
    }

    @Override
    public int getOnlineUserCount() {
        return webSocketHandler.getOnlineUserCount();
    }

    @Override
    public Map<Long, Boolean> checkUsersOnline(Iterable<Long> userIds) {
        Map<Long, Boolean> result = new HashMap<>();

        if (userIds == null) {
            return result;
        }

        for (Long userId : userIds) {
            if (userId != null) {
                result.put(userId, isUserOnline(userId));
            }
        }

        return result;
    }

    /**
     * 更新用户最后活跃时间
     *
     * @param userId 用户ID
     */
    public void updateLastActiveTime(Long userId) {
        if (userId != null) {
            lastActiveTimeCache.put(userId, System.currentTimeMillis());
        }
    }

    /**
     * 获取用户最后活跃时间
     *
     * @param userId 用户ID
     * @return 最后活跃时间戳（毫秒），如果用户从未活跃则返回 null
     */
    public Long getLastActiveTime(Long userId) {
        return lastActiveTimeCache.get(userId);
    }
}
