package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.UpdatePasswordDTO;
import com.xx.xianqijava.dto.UserLoginDTO;
import com.xx.xianqijava.dto.UserRegisterDTO;
import com.xx.xianqijava.dto.UserUpdateDTO;
import com.xx.xianqijava.service.UserService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.UserCenterVO;
import com.xx.xianqijava.vo.UserInfoVO;
import com.xx.xianqijava.vo.UserLoginVO;
import com.xx.xianqijava.vo.UserRegisterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录请求, username={}", loginDTO.getUsername());
        UserLoginVO result = userService.login(loginDTO);
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
     * 获取用户信用积分
     */
    @GetMapping("/{userId}/credit")
    @Operation(summary = "获取用户信用积分")
    public Result<Integer> getUserCreditScore(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        Integer creditScore = userService.getUserCreditScore(userId);
        return Result.success(creditScore);
    }
}
