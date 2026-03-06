package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.*;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.service.FileUploadService;
import com.xx.xianqijava.service.UserService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.PayPasswordCheckVO;
import com.xx.xianqijava.vo.PrivacySettingsVO;
import com.xx.xianqijava.vo.UserCenterVO;
import com.xx.xianqijava.vo.UserInfoVO;
import com.xx.xianqijava.vo.UserLoginVO;
import com.xx.xianqijava.vo.UserRegisterVO;
import com.xx.xianqijava.service.LoginDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户控制器
 */
@Slf4j
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadService fileUploadService;
    private final LoginDeviceService loginDeviceService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserRegisterVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("用户注册请求, username={}", registerDTO.getUsername());
        UserRegisterVO result = userService.register(registerDTO);
        return Result.success("注册成功", result);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO,
                                     HttpServletRequest request) {
        log.info("用户登录请求, username={}", loginDTO.getUsername());
        UserLoginVO result = userService.login(loginDTO);

        // 记录登录设备信息
        try {
            String deviceIdentifier = getDeviceIdentifier(request);
            String deviceName = getDeviceName(request);
            String deviceType = getDeviceType(request);
            String platform = getPlatform(request);
            String ip = getClientIP(request);

            loginDeviceService.recordOrUpdateLoginDevice(
                result.getUserId(),
                deviceIdentifier,
                deviceName,
                deviceType,
                platform,
                ip
            );
            log.info("登录设备记录成功, userId={}, deviceType={}", result.getUserId(), deviceType);
        } catch (Exception e) {
            log.error("记录登录设备失败, userId={}", result.getUserId(), e);
            // 不影响登录流程，只记录错误日志
        }

        return Result.success("登录成功", result);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取用户信息, userId={}", userId);
        UserInfoVO result = userService.getUserInfo(userId);
        return Result.success(result);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<UserInfoVO> updateUserInfo(@Valid @RequestBody UserUpdateDTO updateDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新用户信息, userId={}", userId);
        UserInfoVO result = userService.updateUserInfo(userId, updateDTO);
        return Result.success("更新成功", result);
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO passwordDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("修改密码, userId={}", userId);
        userService.updatePassword(userId, passwordDTO);
        return Result.success("密码修改成功");
    }

    /**
     * 更新头像（通过URL）
     */
    @Operation(summary = "更新头像（通过URL）")
    @PutMapping("/avatar")
    public Result<UserInfoVO> updateAvatar(@Valid @RequestBody UpdateAvatarDTO avatarDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新头像, userId={}", userId);
        UserInfoVO result = userService.updateAvatar(userId, avatarDTO);
        return Result.success("头像更新成功", result);
    }

    /**
     * 上传并更新头像（文件上传）
     */
    @Operation(summary = "上传并更新头像（文件上传）")
    @PostMapping("/avatar")
    public Result<UserInfoVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("上传头像, userId={}, filename={}", userId, file.getOriginalFilename());

        // 上传文件获取URL（使用兼容方法返回String）
        String avatarUrl = fileUploadService.uploadImageReturnUrl(file);

        // 更新用户头像
        UpdateAvatarDTO avatarDTO = new UpdateAvatarDTO();
        avatarDTO.setAvatar(avatarUrl);
        UserInfoVO result = userService.updateAvatar(userId, avatarDTO);

        return Result.success("头像上传成功", result);
    }

    /**
     * 获取用户中心数据
     */
    @Operation(summary = "获取用户中心数据")
    @GetMapping("/center")
    public Result<UserCenterVO> getUserCenter() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取用户中心数据, userId={}", userId);
        UserCenterVO result = userService.getUserCenterData(userId);
        return Result.success(result);
    }

    /**
     * 获取用户统计数据
     */
    @Operation(summary = "获取用户统计数据")
    @GetMapping("/stats")
    public Result<com.xx.xianqijava.vo.UserStatsVO> getUserStats() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取用户统计数据, userId={}", userId);
        com.xx.xianqijava.vo.UserStatsVO result = userService.getUserStats(userId);
        return Result.success(result);
    }

    /**
     * 获取用户信用积分
     */
    @GetMapping("/{userId}/credit")
    @Operation(summary = "获取用户信用积分")
    public Result<Integer> getUserCreditScore(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        Integer creditScore = userService.getUserCreditScore(userId);
        return Result.success(creditScore);
    }

    /**
     * 更新用户位置信息
     */
    @Operation(summary = "更新用户位置信息")
    @PutMapping("/location")
    public Result<Void> updateUserLocation(@Valid @RequestBody UpdateLocationDTO locationDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新用户位置信息, userId={}", userId);
        userService.updateUserLocation(userId, locationDTO);
        return Result.success("位置更新成功");
    }

    /**
     * 获取附近用户列表
     */
    @Operation(summary = "获取附近用户列表")
    @GetMapping("/nearby")
    public Result<List<User>> getNearbyUsers() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取附近用户列表, userId={}", userId);
        List<User> nearbyUsers = userService.getNearbyUsers(userId);
        return Result.success(nearbyUsers);
    }

    /**
     * 发送验证码
     */
    @Operation(summary = "发送验证码")
    @PostMapping("/send-code")
    public Result<Void> sendVerifyCode(@RequestParam("phone") String phone,
                                       @RequestParam(value = "type", defaultValue = "login") String type) {
        log.info("发送验证码请求, phone={}, type={}", phone, type);
        userService.sendVerifyCode(phone, type);
        return Result.success("验证码发送成功");
    }

    /**
     * 验证验证码（用于重置密码前的验证）
     */
    @Operation(summary = "验证验证码")
    @PostMapping("/verify-code")
    public Result<Boolean> verifyCode(@RequestParam("phone") String phone,
                                      @RequestParam("code") String code) {
        log.info("验证验证码请求, phone={}", phone);
        boolean isValid = userService.verifyCode(phone, code);
        return Result.success(isValid);
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestParam("phone") String phone,
                                      @RequestParam("code") String code,
                                      @RequestParam("newPassword") String newPassword) {
        log.info("重置密码请求, phone={}", phone);
        userService.resetPassword(phone, code, newPassword);
        return Result.success("密码重置成功");
    }

    /**
     * 手机号验证码登录
     */
    @Operation(summary = "手机号验证码登录")
    @PostMapping("/login/phone")
    public Result<UserLoginVO> loginByPhone(@RequestParam("phone") String phone,
                                            @RequestParam("code") String code,
                                            HttpServletRequest request) {
        log.info("手机号验证码登录请求, phone={}", phone);
        UserLoginVO result = userService.loginByPhone(phone, code);

        // 记录登录设备信息
        try {
            String deviceIdentifier = getDeviceIdentifier(request);
            String deviceName = getDeviceName(request);
            String deviceType = getDeviceType(request);
            String platform = getPlatform(request);
            String ip = getClientIP(request);

            loginDeviceService.recordOrUpdateLoginDevice(
                result.getUserId(),
                deviceIdentifier,
                deviceName,
                deviceType,
                platform,
                ip
            );
            log.info("登录设备记录成功, userId={}, deviceType={}", result.getUserId(), deviceType);
        } catch (Exception e) {
            log.error("记录登录设备失败, userId={}", result.getUserId(), e);
            // 不影响登录流程，只记录错误日志
        }

        return Result.success("登录成功", result);
    }

    /**
     * 微信授权登录
     */
    @Operation(summary = "微信授权登录")
    @PostMapping("/login/wechat")
    public Result<UserLoginVO> loginByWechat(@RequestParam("code") String code,
                                              @RequestParam(value = "nickname", required = false) String nickname,
                                              @RequestParam(value = "avatar", required = false) String avatar) {
        log.info("微信授权登录请求, code={}", code);
        // TODO: 对接微信开放平台验证code并获取用户信息
        // 当前实现：使用模拟数据返回，实际使用时需要对接微信API
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "微信登录功能暂未开放，请使用其他登录方式");
    }

    /**
     * QQ授权登录
     */
    @Operation(summary = "QQ授权登录")
    @PostMapping("/login/qq")
    public Result<UserLoginVO> loginByQQ(@RequestParam("code") String code,
                                         @RequestParam(value = "nickname", required = false) String nickname,
                                         @RequestParam(value = "avatar", required = false) String avatar) {
        log.info("QQ授权登录请求, code={}", code);
        // TODO: 对接QQ互联平台验证code并获取用户信息
        // 当前实现：使用模拟数据返回，实际使用时需要对接QQ API
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "QQ登录功能暂未开放，请使用其他登录方式");
    }

    // ==================== 账号安全相关接口 ====================

    /**
     * 绑定手机号
     */
    @Operation(summary = "绑定手机号")
    @PostMapping("/bind-phone")
    public Result<Void> bindPhone(@Valid @RequestBody BindPhoneDTO bindPhoneDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("绑定手机号请求, userId={}, phone={}", userId, bindPhoneDTO.getPhone());
        userService.bindPhone(userId, bindPhoneDTO);
        return Result.success("手机号绑定成功");
    }

    /**
     * 更换手机号
     */
    @Operation(summary = "更换手机号")
    @PostMapping("/change-phone")
    public Result<Void> changePhone(@Valid @RequestBody ChangePhoneDTO changePhoneDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更换手机号请求, userId={}", userId);
        userService.changePhone(userId, changePhoneDTO);
        return Result.success("手机号更换成功");
    }

    /**
     * 检查是否设置支付密码
     */
    @Operation(summary = "检查是否设置支付密码")
    @GetMapping("/pay-password/check")
    public Result<PayPasswordCheckVO> checkPayPassword() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("检查支付密码, userId={}", userId);
        boolean hasPayPassword = userService.hasPayPassword(userId);
        return Result.success(new PayPasswordCheckVO(hasPayPassword));
    }

    /**
     * 设置支付密码
     */
    @Operation(summary = "设置支付密码")
    @PostMapping("/pay-password/set")
    public Result<Void> setPayPassword(@Valid @RequestBody SetPayPasswordDTO setPasswordDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("设置支付密码请求, userId={}", userId);
        userService.setPayPassword(userId, setPasswordDTO);
        return Result.success("支付密码设置成功");
    }

    /**
     * 修改支付密码
     */
    @Operation(summary = "修改支付密码")
    @PostMapping("/pay-password/change")
    public Result<Void> changePayPassword(@Valid @RequestBody ChangePayPasswordDTO changePasswordDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("修改支付密码请求, userId={}", userId);
        userService.changePayPassword(userId, changePasswordDTO);
        return Result.success("支付密码修改成功");
    }

    /**
     * 重置支付密码
     */
    @Operation(summary = "重置支付密码")
    @PostMapping("/pay-password/reset")
    public Result<Void> resetPayPassword(@Valid @RequestBody ResetPayPasswordDTO resetPasswordDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("重置支付密码请求, userId={}", userId);
        userService.resetPayPassword(userId, resetPasswordDTO);
        return Result.success("支付密码重置成功");
    }

    /**
     * 验证支付密码
     */
    @Operation(summary = "验证支付密码")
    @PostMapping("/pay-password/verify")
    public Result<Boolean> verifyPayPassword(@Valid @RequestBody VerifyPayPasswordDTO verifyPasswordDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("验证支付密码请求, userId={}", userId);
        boolean isValid = userService.verifyPayPassword(userId, verifyPasswordDTO.getPassword());
        return Result.success(isValid);
    }

    /**
     * 获取隐私设置
     */
    @Operation(summary = "获取隐私设置")
    @GetMapping("/privacy-settings")
    public Result<PrivacySettingsVO> getPrivacySettings() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取隐私设置, userId={}", userId);
        PrivacySettingsVO settings = userService.getPrivacySettings(userId);
        return Result.success(settings);
    }

    /**
     * 更新隐私设置
     */
    @Operation(summary = "更新隐私设置")
    @PutMapping("/privacy-settings")
    public Result<Void> updatePrivacySettings(@Valid @RequestBody UpdatePrivacySettingsDTO settingsDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新隐私设置, userId={}", userId);
        userService.updatePrivacySettings(userId, settingsDTO);
        return Result.success("隐私设置更新成功");
    }

    /**
     * 注销账号
     */
    @Operation(summary = "注销账号")
    @PostMapping("/delete-account")
    public Result<Void> deleteAccount(@Valid @RequestBody DeleteAccountDTO deleteAccountDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("注销账号请求, userId={}", userId);
        userService.deleteAccount(userId, deleteAccountDTO.getPassword());
        return Result.success("账号注销成功");
    }

    // ==================== 登录设备管理接口 ====================

    /**
     * 获取登录设备列表
     */
    @Operation(summary = "获取登录设备列表")
    @GetMapping("/login-devices")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<com.xx.xianqijava.vo.LoginDeviceVO>> getLoginDevices(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取登录设备列表, userId={}, page={}, size={}", userId, page, size);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.xx.xianqijava.entity.LoginDevice> pageParam =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        var devicePage = loginDeviceService.getUserLoginDevices(userId, pageParam);

        return Result.success(devicePage);
    }

    /**
     * 移除登录设备
     */
    @Operation(summary = "移除登录设备")
    @DeleteMapping("/login-devices/{deviceId}")
    public Result<Void> removeLoginDevice(
            @Parameter(description = "设备ID") @PathVariable("deviceId") Long deviceId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("移除登录设备, userId={}, deviceId={}", userId, deviceId);
        loginDeviceService.removeLoginDevice(userId, deviceId);
        return Result.success("设备移除成功");
    }

    /**
     * 移除所有其他设备
     */
    @Operation(summary = "移除所有其他设备")
    @PostMapping("/login-devices/remove-all")
    public Result<Void> removeAllOtherDevices(
            @Parameter(description = "当前设备ID") @RequestParam("deviceId") Long deviceId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("移除所有其他设备, userId={}, currentDeviceId={}", userId, deviceId);
        loginDeviceService.removeAllOtherDevices(userId, deviceId);
        return Result.success("其他设备已移除");
    }

    // ==================== 主题设置接口 ====================

    /**
     * 获取用户主题设置
     */
    @Operation(summary = "获取用户主题设置")
    @GetMapping("/theme")
    public Result<com.xx.xianqijava.vo.ThemeConfigVO> getThemeSettings() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取用户主题设置, userId={}", userId);
        com.xx.xianqijava.vo.ThemeConfigVO themeConfig = userService.getUserThemeConfig(userId);
        return Result.success(themeConfig);
    }

    /**
     * 更新用户主题设置
     */
    @Operation(summary = "更新用户主题设置")
    @PutMapping("/theme")
    public Result<Void> updateThemeSettings(
            @Parameter(description = "主题") @RequestParam(value = "theme", required = false) String theme,
            @Parameter(description = "自动深色模式") @RequestParam(value = "autoDarkMode", required = false) Boolean autoDarkMode,
            @Parameter(description = "字体大小") @RequestParam(value = "fontSize", required = false) Integer fontSize) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新用户主题设置, userId={}, theme={}, autoDarkMode={}, fontSize={}", userId, theme, autoDarkMode, fontSize);
        userService.updateUserThemeConfig(userId, theme, autoDarkMode, fontSize);
        return Result.success("主题设置已更新");
    }

    // ==================== 设备信息提取辅助方法 ====================

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取设备唯一标识
     */
    private String getDeviceIdentifier(HttpServletRequest request) {
        // 优先使用请求头中的设备ID
        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId != null && !deviceId.isEmpty()) {
            return deviceId;
        }

        // 使用 User-Agent 作为备用标识
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            // 简单的哈希处理
            return String.valueOf(userAgent.hashCode());
        }

        // 最后使用 IP 作为标识
        return getClientIP(request);
    }

    /**
     * 获取设备名称
     */
    private String getDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "未知设备";
        }

        // 解析 User-Agent 获取设备名称
        if (userAgent.contains("iPhone")) {
            return "iPhone";
        } else if (userAgent.contains("iPad")) {
            return "iPad";
        } else if (userAgent.contains("Android")) {
            // 尝试提取Android设备型号
            int start = userAgent.indexOf("Android");
            if (start != -1) {
                int end = userAgent.indexOf(";", start);
                if (end != -1) {
                    String model = userAgent.substring(start, end);
                    return model.replace("Android ", "Android ");
                }
            }
            return "Android设备";
        } else if (userAgent.contains("Mac")) {
            return "Mac电脑";
        } else if (userAgent.contains("Windows")) {
            return "Windows电脑";
        } else if (userAgent.contains("Linux")) {
            return "Linux电脑";
        } else if (userAgent.contains("microMessenger")) {
            return "微信浏览器";
        } else if (userAgent.contains("MiniProgram")) {
            return "小程序";
        }

        return "未知设备";
    }

    /**
     * 获取设备类型
     */
    private String getDeviceType(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "web";
        }

        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "ios";
        } else if (userAgent.contains("Android")) {
            return "android";
        } else if (userAgent.contains("MiniProgram") || userAgent.contains("miniProgram")) {
            return "miniapp";
        }

        return "web";
    }

    /**
     * 获取平台信息
     */
    private String getPlatform(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Web";
        }

        if (userAgent.contains("iPhone")) {
            return "iOS";
        } else if (userAgent.contains("iPad")) {
            return "iOS";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("Mac")) {
            return "macOS";
        } else if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        } else if (userAgent.contains("microMessenger")) {
            return "微信";
        } else if (userAgent.contains("MiniProgram")) {
            return "小程序";
        }

        return "Web";
    }
}
