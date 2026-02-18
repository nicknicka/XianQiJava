package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
}
