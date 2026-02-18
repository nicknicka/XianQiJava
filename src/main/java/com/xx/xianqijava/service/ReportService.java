package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.ReportCreateDTO;
import com.xx.xianqijava.entity.Report;
import com.xx.xianqijava.vo.ReportVO;

/**
 * 举报服务接口
 */
public interface ReportService extends IService<Report> {

    /**
     * 创建举报
     *
     * @param createDTO 举报信息
     * @param reporterId 举报人ID
     * @return 举报VO
     */
    ReportVO createReport(ReportCreateDTO createDTO, Long reporterId);

    /**
     * 获取我的举报列表
     *
     * @param reporterId 举报人ID
     * @param page       分页参数
     * @return 举报列表
     */
    IPage<ReportVO> getMyReports(Long reporterId, Page<Report> page);
}
