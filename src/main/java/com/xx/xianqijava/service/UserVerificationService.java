package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.UserVerificationDTO;
import com.xx.xianqijava.dto.VerificationAuditDTO;
import com.xx.xianqijava.entity.UserVerification;
import com.xx.xianqijava.vo.UserVerificationVO;

/**
 * 用户实名认证服务接口
 */
public interface UserVerificationService extends IService<UserVerification> {

    /**
     * 提交实名认证
     *
     * @param verificationDTO 认证信息
     * @param userId          用户ID
     * @return 认证记录VO
     */
    UserVerificationVO submitVerification(UserVerificationDTO verificationDTO, Long userId);

    /**
     * 审核实名认证
     *
     * @param auditDTO   审核信息
     * @param auditorId  审核人ID
     * @return 认证记录VO
     */
    UserVerificationVO auditVerification(VerificationAuditDTO auditDTO, Long auditorId);

    /**
     * 获取我的认证记录
     *
     * @param userId 用户ID
     * @return 认证记录VO
     */
    UserVerificationVO getMyVerification(Long userId);

    /**
     * 获取认证记录详情
     *
     * @param verificationId 认证ID
     * @return 认证记录VO
     */
    UserVerificationVO getVerificationDetail(Long verificationId);

    /**
     * 获取待审核的认证列表
     *
     * @param page 分页参数
     * @return 认证记录列表
     */
    IPage<UserVerificationVO> getPendingVerifications(Page<UserVerification> page);

    /**
     * 获取所有认证记录列表（管理员）
     *
     * @param page  分页参数
     * @param status 状态筛选
     * @return 认证记录列表
     */
    IPage<UserVerificationVO> getAllVerifications(Page<UserVerification> page, Integer status);

    /**
     * 重新提交认证
     *
     * @param verificationDTO 认证信息
     * @param userId          用户ID
     * @return 认证记录VO
     */
    UserVerificationVO resubmitVerification(UserVerificationDTO verificationDTO, Long userId);
}
