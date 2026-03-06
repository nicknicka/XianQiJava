package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.UpdateAvatarDTO;
import com.xx.xianqijava.dto.UpdateLocationDTO;
import com.xx.xianqijava.dto.UpdatePasswordDTO;
import com.xx.xianqijava.dto.UserLoginDTO;
import com.xx.xianqijava.dto.UserRegisterDTO;
import com.xx.xianqijava.dto.UserUpdateDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.vo.UserCenterVO;
import com.xx.xianqijava.vo.UserInfoVO;
import com.xx.xianqijava.vo.UserLoginVO;
import com.xx.xianqijava.vo.UserRegisterVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    UserRegisterVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    UserLoginVO login(UserLoginDTO loginDTO);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    UserInfoVO updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param passwordDTO 密码信息
     */
    void updatePassword(Long userId, UpdatePasswordDTO passwordDTO);

    /**
     * 更新头像
     *
     * @param userId 用户ID
     * @param avatarDTO 头像信息
     * @return 更新后的用户信息
     */
    UserInfoVO updateAvatar(Long userId, UpdateAvatarDTO avatarDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getByPhone(String phone);

    /**
     * 根据学号查询用户
     *
     * @param studentId 学号
     * @return 用户信息
     */
    User getByStudentId(String studentId);

    /**
     * 获取用户中心数据
     *
     * @param userId 用户ID
     * @return 用户中心数据
     */
    UserCenterVO getUserCenterData(Long userId);

    /**
     * 获取用户统计数据
     *
     * @param userId 用户ID
     * @return 用户统计数据
     */
    com.xx.xianqijava.vo.UserStatsVO getUserStats(Long userId);

    /**
     * 获取用户信用积分
     *
     * @param userId 用户ID
     * @return 信用积分
     */
    Integer getUserCreditScore(Long userId);

    /**
     * 更新用户位置信息
     *
     * @param userId 用户ID
     * @param locationDTO 位置信息
     */
    void updateUserLocation(Long userId, UpdateLocationDTO locationDTO);

    /**
     * 获取附近用户列表（同学院/同专业）
     *
     * @param userId 当前用户ID
     * @return 用户列表
     */
    java.util.List<User> getNearbyUsers(Long userId);

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @param type 类型（login/register/reset）
     */
    void sendVerifyCode(String phone, String type);

    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 是否有效
     */
    boolean verifyCode(String phone, String code);

    /**
     * 重置密码
     *
     * @param phone 手机号
     * @param code 验证码
     * @param newPassword 新密码
     */
    void resetPassword(String phone, String code, String newPassword);

    /**
     * 手机号验证码登录
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 登录结果
     */
    UserLoginVO loginByPhone(String phone, String code);

    // ==================== 账号安全相关方法 ====================

    /**
     * 绑定手机号
     *
     * @param userId 用户ID
     * @param bindPhoneDTO 绑定手机号信息
     */
    void bindPhone(Long userId, com.xx.xianqijava.dto.BindPhoneDTO bindPhoneDTO);

    /**
     * 更换手机号
     *
     * @param userId 用户ID
     * @param changePhoneDTO 更换手机号信息
     */
    void changePhone(Long userId, com.xx.xianqijava.dto.ChangePhoneDTO changePhoneDTO);

    /**
     * 检查是否设置支付密码
     *
     * @param userId 用户ID
     * @return 是否已设置
     */
    boolean hasPayPassword(Long userId);

    /**
     * 设置支付密码
     *
     * @param userId 用户ID
     * @param setPasswordDTO 设置支付密码信息
     */
    void setPayPassword(Long userId, com.xx.xianqijava.dto.SetPayPasswordDTO setPasswordDTO);

    /**
     * 修改支付密码
     *
     * @param userId 用户ID
     * @param changePasswordDTO 修改支付密码信息
     */
    void changePayPassword(Long userId, com.xx.xianqijava.dto.ChangePayPasswordDTO changePasswordDTO);

    /**
     * 重置支付密码
     *
     * @param userId 用户ID
     * @param resetPasswordDTO 重置支付密码信息
     */
    void resetPayPassword(Long userId, com.xx.xianqijava.dto.ResetPayPasswordDTO resetPasswordDTO);

    /**
     * 验证支付密码
     *
     * @param userId 用户ID
     * @param password 支付密码
     * @return 是否正确
     */
    boolean verifyPayPassword(Long userId, String password);

    /**
     * 获取隐私设置
     *
     * @param userId 用户ID
     * @return 隐私设置
     */
    com.xx.xianqijava.vo.PrivacySettingsVO getPrivacySettings(Long userId);

    /**
     * 更新隐私设置
     *
     * @param userId 用户ID
     * @param settingsDTO 隐私设置信息
     */
    void updatePrivacySettings(Long userId, com.xx.xianqijava.dto.UpdatePrivacySettingsDTO settingsDTO);

    /**
     * 注销账号
     *
     * @param userId 用户ID
     * @param password 登录密码（用于验证）
     */
    void deleteAccount(Long userId, String password);

    // ==================== 主题设置相关方法 ====================

    /**
     * 获取用户主题配置
     *
     * @param userId 用户ID
     * @return 主题配置
     */
    com.xx.xianqijava.vo.ThemeConfigVO getUserThemeConfig(Long userId);

    /**
     * 更新用户主题配置
     *
     * @param userId       用户ID
     * @param theme        主题
     * @param autoDarkMode 自动深色模式
     * @param fontSize     字体大小
     */
    void updateUserThemeConfig(Long userId, String theme, Boolean autoDarkMode, Integer fontSize);
}
