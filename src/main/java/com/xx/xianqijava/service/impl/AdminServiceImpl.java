package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.dto.admin.AdminLoginDTO;
import com.xx.xianqijava.entity.Admin;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.AdminMapper;
import com.xx.xianqijava.security.SecurityContextHolder;
import com.xx.xianqijava.service.AdminService;
import com.xx.xianqijava.util.AdminJwtUtil;
import com.xx.xianqijava.vo.admin.AdminInfoVO;
import com.xx.xianqijava.vo.admin.AdminLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理员服务实现类
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AdminJwtUtil adminJwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminLoginVO login(AdminLoginDTO dto) {
        log.info("管理员登录, username={}", dto.getUsername());

        // 1. 查询管理员
        Admin admin = this.lambdaQuery()
                .eq(Admin::getUsername, dto.getUsername())
                .one();

        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查账号状态
        if (admin.getIsActive() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 4. 生成Token
        String token = adminJwtUtil.generateToken(
                admin.getId(),
                admin.getUsername(),
                admin.getNickname()
        );

        // 5. 构建返回数据
        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        vo.setAdminInfo(convertToVO(admin));

        log.info("管理员登录成功, username={}, adminId={}", admin.getUsername(), admin.getId());

        return vo;
    }

    @Override
    public AdminInfoVO getAdminInfo(Long adminId) {
        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        return convertToVO(admin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLoginInfo(Long adminId, String ip) {
        this.lambdaUpdate()
                .eq(Admin::getId, adminId)
                .set(Admin::getLastLoginTime, LocalDateTime.now())
                .set(Admin::getLastLoginIp, ip)
                .update();

        log.info("更新管理员最后登录信息, adminId={}, ip={}", adminId, ip);
    }

    /**
     * 转换为AdminInfoVO
     */
    private AdminInfoVO convertToVO(Admin admin) {
        AdminInfoVO vo = new AdminInfoVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setAvatar(admin.getAvatar());
        vo.setIsActive(admin.getIsActive());
        vo.setLastLoginTime(admin.getLastLoginTime());
        vo.setLastLoginIp(admin.getLastLoginIp());
        return vo;
    }
}
