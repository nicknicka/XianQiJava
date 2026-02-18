package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.UserVerificationDTO;
import com.xx.xianqijava.dto.VerificationAuditDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserVerification;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.mapper.UserVerificationMapper;
import com.xx.xianqijava.service.UserVerificationService;
import com.xx.xianqijava.vo.UserVerificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户实名认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVerificationServiceImpl extends ServiceImpl<UserVerificationMapper, UserVerification>
        implements UserVerificationService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVerificationVO submitVerification(UserVerificationDTO verificationDTO, Long userId) {
        log.info("提交实名认证, userId={}, realName={}", userId, verificationDTO.getRealName());

        // 1. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户不存在");
        }

        // 2. 检查是否已实名认证
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您已完成实名认证");
        }

        // 3. 检查是否有待审核的认证记录
        LambdaQueryWrapper<UserVerification> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(UserVerification::getUserId, userId)
                .eq(UserVerification::getStatus, 0); // 待审核
        long pendingCount = count(pendingWrapper);
        if (pendingCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "有待审核的认证记录，请等待审核");
        }

        // 4. 创建认证记录
        UserVerification verification = new UserVerification();
        verification.setUserId(userId);
        verification.setRealName(verificationDTO.getRealName());
        verification.setIdCard(verificationDTO.getIdCard());
        verification.setStudentCardImage(verificationDTO.getStudentCardImage());
        verification.setIdCardFrontImage(verificationDTO.getIdCardFrontImage());
        verification.setIdCardBackImage(verificationDTO.getIdCardBackImage());
        verification.setStatus(0); // 待审核

        boolean saved = save(verification);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "认证记录创建失败");
        }

        log.info("实名认证提交成功, verificationId={}", verification.getVerificationId());
        return convertToVO(verification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVerificationVO auditVerification(VerificationAuditDTO auditDTO, Long auditorId) {
        log.info("审核实名认证, verificationId={}, status={}, auditorId={}",
                auditDTO.getVerificationId(), auditDTO.getStatus(), auditorId);

        UserVerification verification = getById(auditDTO.getVerificationId());
        if (verification == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "认证记录不存在");
        }

        // 验证状态（只能审核待审核的记录）
        if (verification.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能审核待审核的记录");
        }

        // 更新认证记录
        verification.setStatus(auditDTO.getStatus());
        verification.setAuditRemark(auditDTO.getAuditRemark());
        verification.setAuditTime(java.time.LocalDateTime.now());
        verification.setAuditorId(auditorId);
        boolean updated = updateById(verification);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "审核失败");
        }

        // 如果审核通过，更新用户实名认证状态和真实姓名
        if (auditDTO.getStatus() == 1) {
            User user = userMapper.selectById(verification.getUserId());
            if (user != null) {
                user.setIsVerified(1);
                user.setRealName(verification.getRealName());
                userMapper.updateById(user);
                log.info("用户实名认证通过, userId={}, realName={}", user.getUserId(), verification.getRealName());
            }
        }

        log.info("实名认证审核完成, verificationId={}, status={}",
                verification.getVerificationId(), verification.getStatus());
        return convertToVO(verification);
    }

    @Override
    public UserVerificationVO getMyVerification(Long userId) {
        log.info("查询我的认证记录, userId={}", userId);

        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVerification::getUserId, userId)
                .orderByDesc(UserVerification::getCreateTime)
                .last("LIMIT 1");

        UserVerification verification = getOne(wrapper);
        if (verification == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未找到认证记录");
        }

        return convertToVO(verification);
    }

    @Override
    public UserVerificationVO getVerificationDetail(Long verificationId) {
        UserVerification verification = getById(verificationId);
        if (verification == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "认证记录不存在");
        }
        return convertToVO(verification);
    }

    @Override
    public IPage<UserVerificationVO> getPendingVerifications(Page<UserVerification> page) {
        log.info("查询待审核的认证列表, page={}", page.getCurrent());

        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVerification::getStatus, 0) // 待审核
                .orderByAsc(UserVerification::getCreateTime);

        IPage<UserVerification> verificationPage = page(page, wrapper);
        return verificationPage.convert(this::convertToVO);
    }

    @Override
    public IPage<UserVerificationVO> getAllVerifications(Page<UserVerification> page, Integer status) {
        log.info("查询所有认证列表, page={}, status={}", page.getCurrent(), status);

        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(UserVerification::getStatus, status);
        }
        wrapper.orderByDesc(UserVerification::getCreateTime);

        IPage<UserVerification> verificationPage = page(page, wrapper);
        return verificationPage.convert(this::convertToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVerificationVO resubmitVerification(UserVerificationDTO verificationDTO, Long userId) {
        log.info("重新提交实名认证, userId={}, realName={}", userId, verificationDTO.getRealName());

        // 1. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户不存在");
        }

        // 2. 检查是否已实名认证
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您已完成实名认证，无需重新提交");
        }

        // 3. 查询最新的认证记录
        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVerification::getUserId, userId)
                .orderByDesc(UserVerification::getCreateTime)
                .last("LIMIT 1");
        UserVerification lastVerification = getOne(wrapper);

        // 4. 验证最新记录状态（只有被拒绝的才能重新提交）
        if (lastVerification == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "没有认证记录，请使用首次提交接口");
        }
        if (lastVerification.getStatus() != 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前认证记录状态不允许重新提交，只能重新提交被拒绝的认证");
        }

        // 5. 创建新的认证记录
        UserVerification verification = new UserVerification();
        verification.setUserId(userId);
        verification.setRealName(verificationDTO.getRealName());
        verification.setIdCard(verificationDTO.getIdCard());
        verification.setStudentCardImage(verificationDTO.getStudentCardImage());
        verification.setIdCardFrontImage(verificationDTO.getIdCardFrontImage());
        verification.setIdCardBackImage(verificationDTO.getIdCardBackImage());
        verification.setStatus(0); // 待审核

        boolean saved = save(verification);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "认证记录创建失败");
        }

        log.info("实名认证重新提交成功, verificationId={}", verification.getVerificationId());
        return convertToVO(verification);
    }

    /**
     * 转换为VO
     */
    private UserVerificationVO convertToVO(UserVerification verification) {
        UserVerificationVO vo = new UserVerificationVO();
        BeanUtil.copyProperties(verification, vo);

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(verification.getStatus()));

        // 身份证号脱敏
        if (verification.getIdCard() != null) {
            vo.setIdCard(DesensitizedUtil.idCardNum(verification.getIdCard(), 4, 4));
        }

        // 设置时间
        if (verification.getCreateTime() != null) {
            vo.setCreateTime(verification.getCreateTime().toString());
        }
        if (verification.getAuditTime() != null) {
            vo.setAuditTime(verification.getAuditTime().toString());
        }

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "审核通过";
            case 2:
                return "审核拒绝";
            default:
                return "未知状态";
        }
    }
}
