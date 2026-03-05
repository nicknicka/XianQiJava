package com.xx.xianqijava.service;

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
     * 审核学生认证
     *
     * @param authId 认证ID
     * @param auditorId 审核人ID
     * @param status 审核状态：2-通过 3-拒绝
     * @param rejectReason 拒绝原因
     */
    void auditAuth(Long authId, Long auditorId, Integer status, String rejectReason);
}
