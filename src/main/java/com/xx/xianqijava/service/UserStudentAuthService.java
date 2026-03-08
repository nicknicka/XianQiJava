package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.StudentAuthSubmitDTO;
import com.xx.xianqijava.entity.UserStudentAuth;
import com.xx.xianqijava.vo.StudentAuthVO;

/**
 * 用户学生认证服务接口
 */
public interface UserStudentAuthService extends IService<UserStudentAuth> {

    /**
     * 提交学生认证
     *
     * @param userId 用户ID
     * @param submitDTO 认证信息
     * @return 认证记录ID
     */
    Long submitAuth(Long userId, StudentAuthSubmitDTO submitDTO);

    /**
     * 获取用户学生认证信息
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    StudentAuthVO getAuthInfo(Long userId);

    /**
     * 获取学生认证详情（管理员）
     *
     * @param authId 认证ID
     * @return 认证详情
     */
    StudentAuthVO getAuthDetail(Long authId);

    /**
     * 获取待审核的学生认证列表（管理员）
     *
     * @param page 分页参数
     * @return 待审核列表
     */
    IPage<StudentAuthVO> getPendingList(Page<UserStudentAuth> page);

    /**
     * 获取所有学生认证列表（管理员）
     *
     * @param page 分页参数
     * @param status 状态筛选（可选）
     * @return 认证列表
     */
    IPage<StudentAuthVO> getAllList(Page<UserStudentAuth> page, Integer status);

    /**
     * 审核学生认证
     *
     * @param authId 认证ID
     * @param auditorId 审核人ID
     * @param status 审核状态：2-通过 3-拒绝
     * @param rejectReason 拒绝原因
     */
    void auditAuth(Long authId, Long auditorId, Integer status, String rejectReason);
}
