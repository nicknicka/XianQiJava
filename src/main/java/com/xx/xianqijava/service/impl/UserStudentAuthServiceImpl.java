package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.StudentAuthSubmitDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserStudentAuth;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserStudentAuthMapper;
import com.xx.xianqijava.service.UserStudentAuthService;
import com.xx.xianqijava.service.UserService;
import com.xx.xianqijava.vo.StudentAuthVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户学生认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStudentAuthServiceImpl extends ServiceImpl<UserStudentAuthMapper, UserStudentAuth>
        implements UserStudentAuthService {

    private final UserService userService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitAuth(Long userId, StudentAuthSubmitDTO submitDTO) {
        log.info("提交学生认证, userId={}", userId);

        // 1. 查询用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 查询是否已有认证记录
        UserStudentAuth existAuth = getOne(new LambdaQueryWrapper<UserStudentAuth>()
                .eq(UserStudentAuth::getUserId, userId));

        // 3. 如果已有认证且已通过，不允许重复提交
        if (existAuth != null && existAuth.getStatus() == 2) {
            throw new BusinessException(ErrorCode.AUTH_ALREADY_APPROVED);
        }

        // 4. 创建或更新认证记录
        UserStudentAuth auth = new UserStudentAuth();
        if (existAuth != null) {
            auth.setId(existAuth.getId());
        }

        auth.setUserId(userId);
        auth.setStudentId(submitDTO.getStudentId());
        auth.setCollege(submitDTO.getCollege());
        auth.setMajor(submitDTO.getMajor());
        // 将图片列表转换为JSON存储
        auth.setStudentCardImages(JSONUtil.toJsonStr(submitDTO.getStudentCardImages()));
        auth.setStatus(1); // 审核中

        // 5. 保存或更新
        boolean saved = saveOrUpdate(auth);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "提交认证失败");
        }

        log.info("提交学生认证成功, authId={}", auth.getId());
        return auth.getId();
    }

    @Override
    public StudentAuthVO getAuthInfo(Long userId) {
        log.info("获取学生认证信息, userId={}", userId);

        // 1. 查询认证记录
        UserStudentAuth auth = getOne(new LambdaQueryWrapper<UserStudentAuth>()
                .eq(UserStudentAuth::getUserId, userId));

        // 2. 如果没有认证记录，返回默认状态
        if (auth == null) {
            StudentAuthVO vo = new StudentAuthVO();
            vo.setUserId(userId);
            vo.setStatus(0); // 未认证
            return vo;
        }

        // 3. 转换为VO
        StudentAuthVO vo = new StudentAuthVO();
        vo.setId(auth.getId());
        vo.setUserId(auth.getUserId());
        vo.setStudentId(auth.getStudentId());
        vo.setCollege(auth.getCollege());
        vo.setMajor(auth.getMajor());
        // 解析JSON图片列表
        if (auth.getStudentCardImages() != null) {
            List<String> images = JSONUtil.toList(auth.getStudentCardImages(), String.class);
            vo.setStudentCardImages(images);
        }
        vo.setStatus(auth.getStatus());
        vo.setRejectReason(auth.getRejectReason());
        vo.setEnrollmentYear(auth.getEnrollmentYear());
        vo.setGraduationYear(auth.getGraduationYear());
        vo.setEducationLevel(auth.getEducationLevel());
        vo.setAuditedAt(auth.getAuditedAt() != null ?
            auth.getAuditedAt().format(FORMATTER) : null);
        vo.setCreatedAt(auth.getCreateTime() != null ?
            auth.getCreateTime().format(FORMATTER) : null);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditAuth(Long authId, Long auditorId, Integer status, String rejectReason) {
        log.info("审核学生认证, authId={}, auditorId={}, status={}", authId, auditorId, status);

        // 1. 查询认证记录
        UserStudentAuth auth = getById(authId);
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

        log.info("审核学生认证成功, authId={}, status={}", authId, status);
    }
}
