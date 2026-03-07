package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.admin.AdminLoginDTO;
import com.xx.xianqijava.entity.Admin;
import com.xx.xianqijava.vo.admin.AdminInfoVO;
import com.xx.xianqijava.vo.admin.AdminLoginVO;

/**
 * 管理员服务接口
 *
 * @author Claude Code
 * @since 2026-03-07
 */
public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     *
     * @param dto 登录信息
     * @return 登录结果（包含Token和管理员信息）
     */
    AdminLoginVO login(AdminLoginDTO dto);

    /**
     * 获取管理员信息
     *
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    AdminInfoVO getAdminInfo(Long adminId);

    /**
     * 更新最后登录信息
     *
     * @param adminId 管理员ID
     * @param ip      IP地址
     */
    void updateLastLoginInfo(Long adminId, String ip);
}
