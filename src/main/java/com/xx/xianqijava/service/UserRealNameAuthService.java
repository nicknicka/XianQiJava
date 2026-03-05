package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.RealNameAuthSubmitDTO;
import com.xx.xianqijava.entity.UserRealNameAuth;
import com.xx.xianqijava.vo.RealNameAuthVO;

/**
 * 用户实名认证服务接口
 */
public interface UserRealNameAuthService extends IService<UserRealNameAuth> {

    /**
     * 提交实名认证
     *
     * @param userId 用户ID
     * @param submitDTO 认证信息
     * @return 认证记录ID
     */
    Long submitAuth(Long userId, RealNameAuthSubmitDTO submitDTO);

    /**
     * 获取用户实名认证信息
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    RealNameAuthVO getAuthInfo(Long userId);

    /**
     * 审核实名认证
     *
     * @param authId 认证ID
     * @param auditorId 审核人ID
     * @param status 审核状态：2-通过 3-拒绝
     * @param rejectReason 拒绝原因
     */
    void auditAuth(Long authId, Long auditorId, Integer status, String rejectReason);
}
