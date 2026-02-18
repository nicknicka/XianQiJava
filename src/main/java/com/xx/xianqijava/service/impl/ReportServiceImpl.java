package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ReportCreateDTO;
import com.xx.xianqijava.entity.Report;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ReportMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ReportService;
import com.xx.xianqijava.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 举报服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO createReport(ReportCreateDTO createDTO, Long reporterId) {
        log.info("创建举报, reporterId={}, reportedUserId={}", reporterId, createDTO.getReportedUserId());

        // 不能举报自己
        if (reporterId.equals(createDTO.getReportedUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能举报自己");
        }

        // 检查被举报人是否存在
        User reportedUser = userMapper.selectById(createDTO.getReportedUserId());
        if (reportedUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "被举报的用户不存在");
        }

        // 创建举报记录
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReportedUserId(createDTO.getReportedUserId());
        report.setConversationId(createDTO.getConversationId());
        report.setMessageId(createDTO.getMessageId());
        report.setReason(createDTO.getReason());
        report.setDescription(createDTO.getDescription());
        report.setEvidenceImages(createDTO.getEvidenceImages());
        report.setStatus(0); // 待处理

        save(report);
        log.info("举报创建成功, reportId={}", report.getReportId());

        return convertToVO(report);
    }

    @Override
    public IPage<ReportVO> getMyReports(Long reporterId, Page<Report> page) {
        log.info("查询我的举报列表, reporterId={}", reporterId);

        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Report::getReporterId, reporterId)
                .orderByDesc(Report::getCreateTime);

        IPage<Report> reportPage = page(page, queryWrapper);
        return reportPage.convert(this::convertToVO);
    }

    /**
     * 转换为VO
     */
    private ReportVO convertToVO(Report report) {
        ReportVO vo = new ReportVO();
        BeanUtil.copyProperties(report, vo);

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(report.getStatus()));

        // 获取举报人信息
        User reporter = userMapper.selectById(report.getReporterId());
        if (reporter != null) {
            vo.setReporterNickname(reporter.getNickname());
            vo.setReporterAvatar(reporter.getAvatar());
        }

        // 获取被举报人信息
        User reportedUser = userMapper.selectById(report.getReportedUserId());
        if (reportedUser != null) {
            vo.setReportedUserNickname(reportedUser.getNickname());
            vo.setReportedUserAvatar(reportedUser.getAvatar());
        }

        return vo;
    }

    /**
     * 获取举报状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "已处理";
            case 2:
                return "已驳回";
            default:
                return "未知状态";
        }
    }
}
