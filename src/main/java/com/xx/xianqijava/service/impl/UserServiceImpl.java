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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

        // 3. 校验学号是否已注册
        existUser = getByStudentId(registerDTO.getStudentId());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.STUDENT_ID_EXISTS);
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

        // 8. 返回注册结果
        UserRegisterVO registerVO = new UserRegisterVO();
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

        // TODO: 统计我的发布数量、订单数量、收藏数量、评价数量
        // 这些统计需要注入对应的Service来查询
        userCenterVO.setProductCount(0);
        userCenterVO.setOrderCount(0);
        userCenterVO.setFavoriteCount(0);
        userCenterVO.setEvaluationCount(0);

        // TODO: 获取最近发布的商品
        userCenterVO.setRecentProducts(null);

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
}
