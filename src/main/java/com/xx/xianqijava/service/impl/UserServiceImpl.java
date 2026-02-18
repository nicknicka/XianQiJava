package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.UserLoginDTO;
import com.xx.xianqijava.dto.UserRegisterDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.UserService;
import com.xx.xianqijava.util.JwtUtil;
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
}
