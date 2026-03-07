package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.UserQueryDTO;
import com.xx.xianqijava.dto.admin.UserUpdateStatusDTO;
import com.xx.xianqijava.vo.admin.UserManageVO;

/**
 * 用户管理服务接口 - 管理端
 */
public interface UserManageService {

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户分页数据
     */
    Page<UserManageVO> getUserList(UserQueryDTO queryDTO);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserManageVO getUserDetail(Long userId);

    /**
     * 更新用户状态（封禁/解封）
     *
     * @param updateDTO 更新DTO
     * @return 是否成功
     */
    Boolean updateUserStatus(UserUpdateStatusDTO updateDTO);

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    UserStatisticsInfo getUserStatistics();
}
