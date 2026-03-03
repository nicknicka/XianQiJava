package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.UpdateLocationDTO;
import com.xx.xianqijava.dto.UpdatePasswordDTO;
import com.xx.xianqijava.dto.UserLoginDTO;
import com.xx.xianqijava.dto.UserRegisterDTO;
import com.xx.xianqijava.dto.UserUpdateDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.UserService;
import com.xx.xianqijava.util.JwtUtil;
import com.xx.xianqijava.vo.UserCenterVO;
import com.xx.xianqijava.vo.UserInfoVO;
import com.xx.xianqijava.vo.UserLoginVO;
import com.xx.xianqijava.vo.UserRegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final com.xx.xianqijava.service.ProductService productService;
    private final com.xx.xianqijava.service.OrderService orderService;
    private final com.xx.xianqijava.service.EvaluationService evaluationService;
    private final com.xx.xianqijava.service.ProductFavoriteService productFavoriteService;
    private final com.xx.xianqijava.service.UserFollowService userFollowService;
    private final com.xx.xianqijava.service.UserPreferenceService userPreferenceService;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterVO register(UserRegisterDTO registerDTO) {
        log.info("用户注册开始, username={}", registerDTO.getUsername());

        // 1. 校验用户名是否已存在
        User existUser = getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 2. 校验手机号是否已注册
        existUser = getByPhone(registerDTO.getPhone());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }

        // 3. 校验学号是否已注册（仅当学号不为空时校验）
        if (StrUtil.isNotBlank(registerDTO.getStudentId())) {
            existUser = getByStudentId(registerDTO.getStudentId());
            if (existUser != null) {
                throw new BusinessException(ErrorCode.STUDENT_ID_EXISTS);
            }
        }

        // 4. 创建用户对象
        User user = new User();
        BeanUtil.copyProperties(registerDTO, user);

        // 5. 设置默认值
        user.setNickname(registerDTO.getUsername()); // 默认昵称为用户名
        user.setCreditScore(100); // 初始信用分数100
        user.setStatus(0); // 正常状态
        user.setIsVerified(0); // 未实名认证

        // 6. 密码加密
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        user.setPassword(encodedPassword);

        // 7. 保存到数据库
        boolean saved = save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "用户注册失败");
        }

        log.info("用户注册成功, userId={}, username={}", user.getUserId(), user.getUsername());

        // 8. 生成Token (注册成功后自动登录)
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());

        // 9. 构建用户信息
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setPhone(user.getPhone());
        userInfo.setStudentId(user.getStudentId());
        userInfo.setRealName(user.getRealName());
        userInfo.setCollege(user.getCollege());
        userInfo.setMajor(user.getMajor());
        userInfo.setCreditScore(user.getCreditScore());
        userInfo.setStatus(user.getStatus());
        userInfo.setIsVerified(user.getIsVerified());
        userInfo.setCreateTime(user.getCreateTime().toString());
        userInfo.setUpdateTime(user.getUpdateTime().toString());

        // 10. 返回注册结果
        UserRegisterVO registerVO = new UserRegisterVO();
        registerVO.setToken(token);
        registerVO.setUserInfo(userInfo);
        registerVO.setUserId(user.getUserId());
        registerVO.setUsername(user.getUsername());
        registerVO.setNickname(user.getNickname());
        registerVO.setCreateTime(user.getCreateTime().toString());

        return registerVO;
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        log.info("用户登录开始, username={}", loginDTO.getUsername());

        // 1. 根据用户名查询用户
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 校验用户状态
        if (user.getStatus() == 1) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 4. 生成Token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 5. 计算Token过期时间
        long expiresIn = System.currentTimeMillis() + jwtExpiration;

        log.info("用户登录成功, userId={}, username={}", user.getUserId(), user.getUsername());

        // 6. 构建返回结果
        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setToken(token);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setUserId(user.getUserId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setPhone(user.getPhone());
        loginVO.setExpiresIn(expiresIn);

        return loginVO;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(user, userInfoVO);
        userInfoVO.setCreateTime(user.getCreateTime().toString());
        userInfoVO.setUpdateTime(user.getUpdateTime().toString());

        return userInfoVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        log.info("更新用户信息, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新字段
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (updateDTO.getPhone() != null) {
            // 校验手机号是否被其他用户使用
            User existUser = getByPhone(updateDTO.getPhone());
            if (existUser != null && !existUser.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getRealName() != null) {
            user.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getCollege() != null) {
            user.setCollege(updateDTO.getCollege());
        }
        if (updateDTO.getMajor() != null) {
            user.setMajor(updateDTO.getMajor());
        }

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新用户信息失败");
        }

        log.info("更新用户信息成功, userId={}", userId);

        return getUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordDTO passwordDTO) {
        log.info("修改密码, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 校验原密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }

        // 新密码不能与原密码相同
        if (passwordDTO.getOldPassword().equals(passwordDTO.getNewPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码不能与原密码相同");
        }

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(passwordDTO.getNewPassword());
        user.setPassword(encodedPassword);

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "修改密码失败");
        }

        log.info("修改密码成功, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateAvatar(Long userId, com.xx.xianqijava.dto.UpdateAvatarDTO avatarDTO) {
        log.info("更新头像, userId={}, avatar={}", userId, avatarDTO.getAvatar());

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setAvatar(avatarDTO.getAvatar());

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新头像失败");
        }

        log.info("更新头像成功, userId={}", userId);

        return getUserInfo(userId);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public User getByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return getOne(wrapper);
    }

    @Override
    public User getByStudentId(String studentId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStudentId, studentId);
        return getOne(wrapper);
    }

    @Override
    public UserCenterVO getUserCenterData(Long userId) {
        log.info("获取用户中心数据, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserCenterVO userCenterVO = new UserCenterVO();
        userCenterVO.setUserId(user.getUserId());
        userCenterVO.setUsername(user.getUsername());
        userCenterVO.setNickname(user.getNickname());
        userCenterVO.setAvatar(user.getAvatar());
        userCenterVO.setCreditScore(user.getCreditScore());

        try {
            // 统计我的发布数量
            int productCount = productService.countByUserId(userId);
            userCenterVO.setProductCount(productCount);

            // 统计我的订单数量（作为买家和卖家）
            int orderCount = orderService.countByUserId(userId);
            userCenterVO.setOrderCount(orderCount);

            // 统计我的收藏数量
            int favoriteCount = productFavoriteService.countByUserId(userId);
            userCenterVO.setFavoriteCount(favoriteCount);

            // 统计收到的评价数量
            int evaluationCount = evaluationService.countByEvaluatedUserId(userId);
            userCenterVO.setEvaluationCount(evaluationCount);

            // 统计关注数量
            int followingCount = userFollowService.countFollowing(userId);
            userCenterVO.setFollowingCount(followingCount);

            // 统计粉丝数量
            int followerCount = userFollowService.countFollowers(userId);
            userCenterVO.setFollowerCount(followerCount);

            // 获取最近发布的商品（最多5个）
            java.util.List<com.xx.xianqijava.vo.ProductVO> recentProducts =
                productService.getRecentProductsByUserId(userId, 5);
            userCenterVO.setRecentProducts(recentProducts);

        } catch (Exception e) {
            log.error("获取用户中心统计数据失败, userId={}", userId, e);
            // 如果统计失败,设置默认值
            userCenterVO.setProductCount(0);
            userCenterVO.setOrderCount(0);
            userCenterVO.setFavoriteCount(0);
            userCenterVO.setEvaluationCount(0);
            userCenterVO.setFollowingCount(0);
            userCenterVO.setFollowerCount(0);
            userCenterVO.setRecentProducts(new java.util.ArrayList<>());
        }

        return userCenterVO;
    }

    @Override
    public Integer getUserCreditScore(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getCreditScore();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserLocation(Long userId, UpdateLocationDTO locationDTO) {
        log.info("更新用户位置信息, userId={}, latitude={}, longitude={}",
                userId, locationDTO.getLatitude(), locationDTO.getLongitude());

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 注意：当前User表没有存储经纬度的字段
        // 位置信息主要用于匹配同学院/同专业的用户
        // UpdateLocationDTO中的地址信息可以在发布商品时使用

        log.info("用户位置信息处理完成, userId={}", userId);
    }

    @Override
    public java.util.List<User> getNearbyUsers(Long userId) {
        log.info("获取附近用户列表, userId={}", userId);

        User currentUser = getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 查找同学院或同专业的用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(User::getUserId, userId) // 排除自己
                .eq(User::getStatus, 0); // 只查询正常用户

        // 优先匹配同学院同专业
        if (StrUtil.isNotBlank(currentUser.getCollege()) && StrUtil.isNotBlank(currentUser.getMajor())) {
            wrapper.and(w -> w.eq(User::getCollege, currentUser.getCollege())
                    .or()
                    .eq(User::getMajor, currentUser.getMajor()));
        } else if (StrUtil.isNotBlank(currentUser.getCollege())) {
            wrapper.eq(User::getCollege, currentUser.getCollege());
        }

        wrapper.orderByDesc(User::getUpdateTime).last("LIMIT 20");

        java.util.List<User> nearbyUsers = list(wrapper);
        log.info("找到附近用户数量, userId={}, count={}", userId, nearbyUsers.size());

        return nearbyUsers;
    }

    @Override
    public void sendVerifyCode(String phone, String type) {
        log.info("发送验证码, phone={}, type={}", phone, type);

        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号格式不正确");
        }

        // 检查是否频繁发送（60秒内只能发送一次）
        String rateLimitKey = "verify_code:rate:" + phone;
        String lastSendTime = redisTemplate.opsForValue().get(rateLimitKey);
        if (lastSendTime != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码发送过于频繁，请60秒后再试");
        }

        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 存储验证码到Redis，5分钟过期
        String verifyCodeKey = "verify_code:" + phone;
        redisTemplate.opsForValue().set(verifyCodeKey, code, 5, TimeUnit.MINUTES);

        // 设置发送频率限制
        redisTemplate.opsForValue().set(rateLimitKey, String.valueOf(System.currentTimeMillis()), 60, TimeUnit.SECONDS);

        // TODO: 实际发送短信（这里需要对接短信服务商）
        // 这里仅打印日志，实际生产环境需要调用短信服务API
        log.info("验证码生成成功, phone={}, code={}, type={}", phone, code, type);
        // smsService.sendVerifyCode(phone, code);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        log.info("验证验证码, phone={}, code={}", phone, code);

        String verifyCodeKey = "verify_code:" + phone;
        String savedCode = redisTemplate.opsForValue().get(verifyCodeKey);

        if (savedCode == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已过期或不存在");
        }

        if (!savedCode.equals(code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不正确");
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String phone, String code, String newPassword) {
        log.info("重置密码, phone={}", phone);

        // 验证验证码
        verifyCode(phone, code);

        // 查找用户
        User user = getByPhone(phone);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "该手机号未注册");
        }

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "密码重置失败");
        }

        // 删除验证码
        String verifyCodeKey = "verify_code:" + phone;
        redisTemplate.delete(verifyCodeKey);

        log.info("密码重置成功, phone={}, userId={}", phone, user.getUserId());
    }

    @Override
    public UserLoginVO loginByPhone(String phone, String code) {
        log.info("手机号验证码登录, phone={}", phone);

        // 验证验证码
        verifyCode(phone, code);

        // 查找用户
        User user = getByPhone(phone);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "该手机号未注册");
        }

        // 校验用户状态
        if (user.getStatus() == 1) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 登录成功后删除验证码
        String verifyCodeKey = "verify_code:" + phone;
        redisTemplate.delete(verifyCodeKey);

        // 生成Token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 计算Token过期时间
        long expiresIn = System.currentTimeMillis() + jwtExpiration;

        log.info("手机号登录成功, userId={}, phone={}", user.getUserId(), phone);

        // 构建返回结果
        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setToken(token);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setUserId(user.getUserId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setPhone(user.getPhone());
        loginVO.setExpiresIn(expiresIn);

        return loginVO;
    }

    // ==================== 账号安全相关方法实现 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, com.xx.xianqijava.dto.BindPhoneDTO bindPhoneDTO) {
        log.info("绑定手机号, userId={}, phone={}", userId, bindPhoneDTO.getPhone());

        // 验证验证码
        verifyCode(bindPhoneDTO.getPhone(), bindPhoneDTO.getCode());

        // 检查手机号是否已被其他用户绑定
        User existUser = getByPhone(bindPhoneDTO.getPhone());
        if (existUser != null && !existUser.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该手机号已被其他用户绑定");
        }

        // 获取当前用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新手机号
        user.setPhone(bindPhoneDTO.getPhone());
        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "手机号绑定失败");
        }

        // 删除验证码
        String verifyCodeKey = "verify_code:" + bindPhoneDTO.getPhone();
        redisTemplate.delete(verifyCodeKey);

        log.info("手机号绑定成功, userId={}, phone={}", userId, bindPhoneDTO.getPhone());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePhone(Long userId, com.xx.xianqijava.dto.ChangePhoneDTO changePhoneDTO) {
        log.info("更换手机号, userId={}", userId);

        // 验证验证码
        verifyCode(changePhoneDTO.getNewPhone(), changePhoneDTO.getCode());

        // 检查新手机号是否已被其他用户绑定
        User existUser = getByPhone(changePhoneDTO.getNewPhone());
        if (existUser != null && !existUser.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该手机号已被其他用户绑定");
        }

        // 获取当前用户并验证原手机号
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!changePhoneDTO.getOldPhone().equals(user.getPhone())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原手机号不正确");
        }

        // 更新手机号
        user.setPhone(changePhoneDTO.getNewPhone());
        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "手机号更换失败");
        }

        // 删除验证码
        String verifyCodeKey = "verify_code:" + changePhoneDTO.getNewPhone();
        redisTemplate.delete(verifyCodeKey);

        log.info("手机号更换成功, userId={}, newPhone={}", userId, changePhoneDTO.getNewPhone());
    }

    @Override
    public boolean hasPayPassword(Long userId) {
        log.info("检查支付密码, userId={}", userId);
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return StrUtil.isNotBlank(user.getPayPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPayPassword(Long userId, com.xx.xianqijava.dto.SetPayPasswordDTO setPasswordDTO) {
        log.info("设置支付密码, userId={}", userId);

        // 验证验证码
        verifyCode(setPasswordDTO.getPhone(), setPasswordDTO.getCode());

        // 验证手机号是否属于当前用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!setPasswordDTO.getPhone().equals(user.getPhone())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号验证失败");
        }

        // 检查是否已设置支付密码
        if (StrUtil.isNotBlank(user.getPayPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您已设置过支付密码，如需修改请使用修改功能");
        }

        // 检查弱密码
        String password = setPasswordDTO.getPassword();
        if (isWeakPayPassword(password)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付密码过于简单，请重新设置");
        }

        // 加密支付密码
        String encodedPassword = passwordEncoder.encode(password);
        user.setPayPassword(encodedPassword);

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "支付密码设置失败");
        }

        // 删除验证码
        String verifyCodeKey = "verify_code:" + setPasswordDTO.getPhone();
        redisTemplate.delete(verifyCodeKey);

        log.info("支付密码设置成功, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePayPassword(Long userId, com.xx.xianqijava.dto.ChangePayPasswordDTO changePasswordDTO) {
        log.info("修改支付密码, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查是否已设置支付密码
        if (StrUtil.isBlank(user.getPayPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您还未设置支付密码");
        }

        // 验证原支付密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPayPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原支付密码不正确");
        }

        // 检查弱密码
        String newPassword = changePasswordDTO.getNewPassword();
        if (isWeakPayPassword(newPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付密码过于简单，请重新设置");
        }

        // 加密新支付密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPayPassword(encodedPassword);

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "支付密码修改失败");
        }

        log.info("支付密码修改成功, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPayPassword(Long userId, com.xx.xianqijava.dto.ResetPayPasswordDTO resetPasswordDTO) {
        log.info("重置支付密码, userId={}", userId);

        // 验证验证码
        verifyCode(resetPasswordDTO.getPhone(), resetPasswordDTO.getCode());

        // 验证手机号是否属于当前用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!resetPasswordDTO.getPhone().equals(user.getPhone())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号验证失败");
        }

        // 检查弱密码
        String newPassword = resetPasswordDTO.getNewPassword();
        if (isWeakPayPassword(newPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "支付密码过于简单，请重新设置");
        }

        // 加密新支付密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPayPassword(encodedPassword);

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "支付密码重置失败");
        }

        // 删除验证码
        String verifyCodeKey = "verify_code:" + resetPasswordDTO.getPhone();
        redisTemplate.delete(verifyCodeKey);

        log.info("支付密码重置成功, userId={}", userId);
    }

    @Override
    public boolean verifyPayPassword(Long userId, String password) {
        log.info("验证支付密码, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (StrUtil.isBlank(user.getPayPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您还未设置支付密码");
        }

        return passwordEncoder.matches(password, user.getPayPassword());
    }

    @Override
    public com.xx.xianqijava.vo.PrivacySettingsVO getPrivacySettings(Long userId) {
        log.info("获取隐私设置, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return new com.xx.xianqijava.vo.PrivacySettingsVO(
            user.getPhoneSearchEnabled() != null ? user.getPhoneSearchEnabled() : 1,
            user.getLocationEnabled() != null ? user.getLocationEnabled() : 1
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacySettings(Long userId, com.xx.xianqijava.dto.UpdatePrivacySettingsDTO settingsDTO) {
        log.info("更新隐私设置, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 只更新非空字段
        if (settingsDTO.getPhoneSearchEnabled() != null) {
            user.setPhoneSearchEnabled(settingsDTO.getPhoneSearchEnabled());
        }
        if (settingsDTO.getLocationEnabled() != null) {
            user.setLocationEnabled(settingsDTO.getLocationEnabled());
        }

        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "隐私设置更新失败");
        }

        log.info("隐私设置更新成功, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId, String password) {
        log.info("注销账号, userId={}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证登录密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录密码不正确");
        }

        // 逻辑删除用户
        user.setDeleted(1);
        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "账号注销失败");
        }

        log.info("账号注销成功, userId={}", userId);
    }

    /**
     * 检查是否为弱支付密码
     * 连续数字：123456, 234567, 345678, 456789, 012345
     * 重复数字：111111, 222222, etc.
     */
    private boolean isWeakPayPassword(String password) {
        if (password == null || password.length() != 6) {
            return true;
        }

        // 连续数字检查
        if ("012345".equals(password) || "123456".equals(password) ||
            "234567".equals(password) || "345678".equals(password) ||
            "456789".equals(password) || "543210".equals(password) ||
            "654321".equals(password) || "765432".equals(password) ||
            "876543".equals(password) || "987654".equals(password)) {
            return true;
        }

        // 重复数字检查
        if (password.matches("^(\\d)\\1{5}$")) {
            return true;
        }

        return false;
    }

    // ==================== 主题设置相关方法实现 ====================

    @Override
    public com.xx.xianqijava.vo.ThemeConfigVO getUserThemeConfig(Long userId) {
        log.info("获取用户主题配置, userId={}", userId);

        // 验证用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 获取用户偏好设置
        com.xx.xianqijava.entity.UserPreference preference =
                userPreferenceService.getOrCreateUserPreference(userId);

        com.xx.xianqijava.vo.ThemeConfigVO vo = new com.xx.xianqijava.vo.ThemeConfigVO();
        vo.setTheme(preference.getTheme() != null ? preference.getTheme() : "light");
        vo.setAutoDarkMode(preference.getAutoDarkMode() != null ? preference.getAutoDarkMode() == 1 : false);
        vo.setFontSize(preference.getFontSize() != null ? preference.getFontSize() : 16);

        // 设置可选主题列表
        vo.setAvailableThemes(getAvailableThemeOptions());

        // 设置可选字体大小列表
        java.util.Map<String, Integer> fontSizes = new java.util.LinkedHashMap<>();
        fontSizes.put("极小", 14);
        fontSizes.put("小", 15);
        fontSizes.put("默认", 16);
        fontSizes.put("大", 17);
        fontSizes.put("极大", 18);
        vo.setAvailableFontSizes(fontSizes);

        // 设置主题颜色
        vo.setThemeColors(getThemeColors(preference.getTheme()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserThemeConfig(Long userId, String theme, Boolean autoDarkMode, Integer fontSize) {
        log.info("更新用户主题配置, userId={}, theme={}, autoDarkMode={}, fontSize={}",
                userId, theme, autoDarkMode, fontSize);

        // 验证用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 委托给 UserPreferenceService 处理
        userPreferenceService.updateThemeConfig(userId, theme, autoDarkMode, fontSize);

        log.info("主题配置更新成功, userId={}", userId);
    }

    /**
     * 获取可用的主题选项列表
     */
    private java.util.List<com.xx.xianqijava.vo.ThemeOptionVO> getAvailableThemeOptions() {
        java.util.List<com.xx.xianqijava.vo.ThemeOptionVO> themes = new java.util.ArrayList<>();

        // 浅色主题
        com.xx.xianqijava.vo.ThemeOptionVO lightTheme = new com.xx.xianqijava.vo.ThemeOptionVO();
        lightTheme.setValue("light");
        lightTheme.setLabel("浅色");
        lightTheme.setIcon("sunny");
        lightTheme.setDefault(true);
        themes.add(lightTheme);

        // 深色主题
        com.xx.xianqijava.vo.ThemeOptionVO darkTheme = new com.xx.xianqijava.vo.ThemeOptionVO();
        darkTheme.setValue("dark");
        darkTheme.setLabel("深色");
        darkTheme.setIcon("moon");
        darkTheme.setDefault(false);
        themes.add(darkTheme);

        // 跟随系统
        com.xx.xianqijava.vo.ThemeOptionVO autoTheme = new com.xx.xianqijava.vo.ThemeOptionVO();
        autoTheme.setValue("auto");
        autoTheme.setLabel("跟随系统");
        autoTheme.setIcon("desktop");
        autoTheme.setDefault(false);
        themes.add(autoTheme);

        return themes;
    }

    /**
     * 获取主题颜色配置
     */
    private com.xx.xianqijava.vo.ThemeColorsVO getThemeColors(String theme) {
        com.xx.xianqijava.vo.ThemeColorsVO colors = new com.xx.xianqijava.vo.ThemeColorsVO();

        if ("dark".equals(theme)) {
            // 深色主题颜色
            colors.setPrimary("#3b82f6");
            colors.setSuccess("#10b981");
            colors.setWarning("#f59e0b");
            colors.setError("#ef4444");
            colors.setBackground("#111827");
            colors.setForeground("#f9fafb");
            colors.setBorder("#374151");
        } else {
            // 浅色主题颜色（默认）
            colors.setPrimary("#3b82f6");
            colors.setSuccess("#10b981");
            colors.setWarning("#f59e0b");
            colors.setError("#ef4444");
            colors.setBackground("#ffffff");
            colors.setForeground("#111827");
            colors.setBorder("#e5e7eb");
        }

        return colors;
    }

    /**
     * 验证主题值是否有效
     */
    private boolean isValidTheme(String theme) {
        return "light".equals(theme) || "dark".equals(theme) || "auto".equals(theme);
    }
}
