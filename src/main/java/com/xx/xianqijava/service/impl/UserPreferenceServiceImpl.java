package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.UserPreference;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserPreferenceMapper;
import com.xx.xianqijava.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户偏好设置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference>
        implements UserPreferenceService {

    private final UserPreferenceMapper userPreferenceMapper;

    @Override
    public UserPreference getUserPreference(Long userId) {
        log.info("获取用户偏好设置, userId={}", userId);

        return userPreferenceMapper.selectOne(
                new LambdaQueryWrapper<UserPreference>()
                        .eq(UserPreference::getUserId, userId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPreference getOrCreateUserPreference(Long userId) {
        log.info("获取或创建用户偏好设置, userId={}", userId);

        UserPreference preference = getUserPreference(userId);

        if (preference == null) {
            preference = new UserPreference();
            preference.setUserId(userId);
            preference.setTheme("light");
            preference.setAutoDarkMode(0);
            preference.setFontSize(16);
            preference.setDeviceType("unknown");
            preference.setLanguage("zh-CN");
            preference.setNotificationEnabled(1);
            preference.setSoundEnabled(1);
            preference.setVibrationEnabled(1);
            try {
                userPreferenceMapper.insert(preference);
            } catch (DuplicateKeyException e) {
                // 首次进入主题页时，读取和更新接口可能并发触发，唯一键冲突后重查即可。
                log.warn("用户偏好设置已被并发创建，重新读取, userId={}", userId);
                preference = getUserPreference(userId);
                if (preference == null) {
                    throw e;
                }
            }
            log.info("创建默认用户偏好设置, userId={}", userId);
        }

        return preference;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThemeConfig(Long userId, String theme, Boolean autoDarkMode, Integer fontSize) {
        log.info("更新主题配置, userId={}, theme={}, autoDarkMode={}, fontSize={}",
                userId, theme, autoDarkMode, fontSize);

        // 验证主题值
        if (theme != null && !isValidTheme(theme)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的主题值");
        }

        // 验证字体大小
        if (fontSize != null && (fontSize < 12 || fontSize > 24)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "字体大小必须在12-24之间");
        }

        UserPreference preference = getOrCreateUserPreference(userId);

        // 更新主题配置
        if (theme != null) {
            preference.setTheme(theme);
        }
        if (autoDarkMode != null) {
            preference.setAutoDarkMode(autoDarkMode ? 1 : 0);
        }
        if (fontSize != null) {
            preference.setFontSize(fontSize);
        }

        userPreferenceMapper.updateById(preference);
        log.info("主题配置更新成功, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotificationSettings(Long userId, Boolean notificationEnabled,
                                           Boolean soundEnabled, Boolean vibrationEnabled) {
        log.info("更新通知设置, userId={}, notification={}, sound={}, vibration={}",
                userId, notificationEnabled, soundEnabled, vibrationEnabled);

        UserPreference preference = getOrCreateUserPreference(userId);

        if (notificationEnabled != null) {
            preference.setNotificationEnabled(notificationEnabled ? 1 : 0);
        }
        if (soundEnabled != null) {
            preference.setSoundEnabled(soundEnabled ? 1 : 0);
        }
        if (vibrationEnabled != null) {
            preference.setVibrationEnabled(vibrationEnabled ? 1 : 0);
        }

        userPreferenceMapper.updateById(preference);
        log.info("通知设置更新成功, userId={}", userId);
    }

    /**
     * 验证主题值是否有效
     */
    private boolean isValidTheme(String theme) {
        return "light".equals(theme) || "dark".equals(theme) || "auto".equals(theme);
    }
}
