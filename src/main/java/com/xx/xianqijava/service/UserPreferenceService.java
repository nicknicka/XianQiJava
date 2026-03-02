package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.UserPreference;

/**
 * 用户偏好设置服务接口
 */
public interface UserPreferenceService extends IService<UserPreference> {

    /**
     * 获取用户偏好设置
     *
     * @param userId 用户ID
     * @return 用户偏好设置
     */
    UserPreference getUserPreference(Long userId);

    /**
     * 获取或创建用户偏好设置
     *
     * @param userId 用户ID
     * @return 用户偏好设置
     */
    UserPreference getOrCreateUserPreference(Long userId);

    /**
     * 更新主题配置
     *
     * @param userId       用户ID
     * @param theme        主题
     * @param autoDarkMode 自动深色模式
     * @param fontSize     字体大小
     */
    void updateThemeConfig(Long userId, String theme, Boolean autoDarkMode, Integer fontSize);

    /**
     * 更新通知设置
     *
     * @param userId               用户ID
     * @param notificationEnabled  通知开关
     * @param soundEnabled         提示音开关
     * @param vibrationEnabled     振动开关
     */
    void updateNotificationSettings(Long userId, Boolean notificationEnabled, Boolean soundEnabled, Boolean vibrationEnabled);
}
