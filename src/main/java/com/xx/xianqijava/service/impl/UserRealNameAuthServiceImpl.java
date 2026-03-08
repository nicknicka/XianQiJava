package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.RealNameAuthSubmitDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserRealNameAuth;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserRealNameAuthMapper;
import com.xx.xianqijava.service.UserRealNameAuthService;
import com.xx.xianqijava.service.UserService;
import com.xx.xianqijava.util.AESUtil;
import com.xx.xianqijava.vo.RealNameAuthVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * 用户实名认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRealNameAuthServiceImpl extends ServiceImpl<UserRealNameAuthMapper, UserRealNameAuth>
        implements UserRealNameAuthService {

    private final UserService userService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitAuth(Long userId, RealNameAuthSubmitDTO submitDTO) {
        log.info("提交实名认证, userId={}", userId);

        // 1. 查询用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 查询是否已有认证记录
        UserRealNameAuth existAuth = getOne(new LambdaQueryWrapper<UserRealNameAuth>()
                .eq(UserRealNameAuth::getUserId, userId));

        // 3. 如果已有认证且已通过，不允许重复提交
        if (existAuth != null && existAuth.getStatus() == 2) {
            throw new BusinessException(ErrorCode.AUTH_ALREADY_APPROVED);
        }

        // 4. 创建或更新认证记录
        UserRealNameAuth auth = new UserRealNameAuth();
        if (existAuth != null) {
            auth.setId(existAuth.getId());
        }

        auth.setUserId(userId);
        auth.setRealName(submitDTO.getRealName());
        // 身份证号加密存储
        auth.setIdCard(AESUtil.encrypt(submitDTO.getIdCard()));
        auth.setStatus(1); // 审核中

        // 5. 保存或更新
        boolean saved = saveOrUpdate(auth);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "提交认证失败");
        }

        log.info("提交实名认证成功, authId={}", auth.getId());
        return auth.getId();
    }

    @Override
    public RealNameAuthVO getAuthInfo(Long userId) {
        log.info("获取实名认证信息, userId={}", userId);

        // 1. 查询认证记录
        UserRealNameAuth auth = getOne(new LambdaQueryWrapper<UserRealNameAuth>()
                .eq(UserRealNameAuth::getUserId, userId));

        // 2. 如果没有认证记录，返回默认状态
        if (auth == null) {
            RealNameAuthVO vo = new RealNameAuthVO();
            vo.setUserId(userId);
            vo.setStatus(0); // 未认证
            return vo;
        }

        // 3. 转换为VO
        RealNameAuthVO vo = new RealNameAuthVO();
        vo.setId(auth.getId());
        vo.setUserId(auth.getUserId());
        vo.setRealName(auth.getRealName());
        // 身份证号脱敏
        vo.setIdCard(maskIdCard(auth.getIdCard()));
        vo.setStatus(auth.getStatus());
        vo.setRejectReason(auth.getRejectReason());
        vo.setAuditedAt(auth.getAuditedAt() != null ?
            auth.getAuditedAt().format(FORMATTER) : null);
        vo.setCreatedAt(auth.getCreateTime() != null ?
            auth.getCreateTime().format(FORMATTER) : null);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditAuth(Long authId, Long auditorId, Integer status, String rejectReason) {
        log.info("审核实名认证, authId={}, auditorId={}, status={}", authId, auditorId, status);

        // 1. 查询认证记录
        UserRealNameAuth auth = getById(authId);
        if (auth == null) {
            throw new BusinessException(ErrorCode.AUTH_NOT_EXISTS);
        }

        // 2. 校验状态
        if (auth.getStatus() != 1) {
            throw new BusinessException(ErrorCode.AUTH_STATUS_ERROR);
        }

        // 3. 更新认证状态
        auth.setStatus(status);
        auth.setAuditedBy(auditorId);
        auth.setAuditedAt(java.time.LocalDateTime.now());
        if (status == 3) {
            auth.setRejectReason(rejectReason);
        }
        updateById(auth);

        log.info("审核实名认证成功, authId={}, status={}", authId, status);
    }

    @Override
    public RealNameAuthVO getAuthDetail(Long authId) {
        log.info("获取实名认证详情, authId={}", authId);

        UserRealNameAuth auth = getById(authId);
        if (auth == null) {
            throw new BusinessException(ErrorCode.AUTH_NOT_EXISTS);
        }

        return convertToVO(auth);
    }

    @Override
    public IPage<RealNameAuthVO> getPendingList(Page<UserRealNameAuth> page) {
        log.info("获取待审核的实名认证列表, page={}", page.getCurrent());

        LambdaQueryWrapper<UserRealNameAuth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRealNameAuth::getStatus, 1) // 审核中
                .orderByAsc(UserRealNameAuth::getCreateTime);

        IPage<UserRealNameAuth> authPage = page(page, wrapper);
        return authPage.convert(this::convertToVO);
    }

    @Override
    public IPage<RealNameAuthVO> getAllList(Page<UserRealNameAuth> page, Integer status) {
        log.info("获取所有实名认证列表, page={}, status={}", page.getCurrent(), status);

        LambdaQueryWrapper<UserRealNameAuth> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(UserRealNameAuth::getStatus, status);
        }
        wrapper.orderByDesc(UserRealNameAuth::getCreateTime);

        IPage<UserRealNameAuth> authPage = page(page, wrapper);
        return authPage.convert(this::convertToVO);
    }

    /**
     * 转换为VO
     */
    private RealNameAuthVO convertToVO(UserRealNameAuth auth) {
        RealNameAuthVO vo = new RealNameAuthVO();
        vo.setId(auth.getId());
        vo.setUserId(auth.getUserId());
        vo.setRealName(auth.getRealName());
        // 身份证号脱敏
        vo.setIdCard(maskIdCard(auth.getIdCard()));
        vo.setStatus(auth.getStatus());
        vo.setRejectReason(auth.getRejectReason());
        vo.setAuditedAt(auth.getAuditedAt() != null ?
            auth.getAuditedAt().format(FORMATTER) : null);
        vo.setCreatedAt(auth.getCreateTime() != null ?
            auth.getCreateTime().format(FORMATTER) : null);

        return vo;
    }

    /**
     * 身份证号脱敏
     */
    private String maskIdCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return "";
        }
        try {
            // 先解密
            String decrypted = AESUtil.decrypt(idCard);
            // 脱敏：保留前6位和后4位
            if (decrypted.length() == 18) {
                return decrypted.substring(0, 6) + "********" + decrypted.substring(14);
            } else if (decrypted.length() == 15) {
                return decrypted.substring(0, 6) + "*****" + decrypted.substring(11);
            }
            return decrypted;
        } catch (Exception e) {
            log.error("身份证号脱敏失败", e);
            return "";
        }
    }
}
