package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.ReportCreateDTO;
import com.xx.xianqijava.entity.Report;
import com.xx.xianqijava.service.ReportService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 举报控制器
 */
@Slf4j
@Tag(name = "举报管理")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 创建举报
     */
    @PostMapping
    @Operation(summary = "创建举报")
    public Result<ReportVO> createReport(@Valid @RequestBody ReportCreateDTO createDTO) {
        Long reporterId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建举报, reporterId={}, reportedUserId={}", reporterId, createDTO.getReportedUserId());
        ReportVO reportVO = reportService.createReport(createDTO, reporterId);
        return Result.success("举报提交成功", reportVO);
    }

    /**
     * 获取我的举报列表
     */
    @GetMapping
    @Operation(summary = "获取我的举报列表")
    public Result<IPage<ReportVO>> getMyReports(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long reporterId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的举报列表, reporterId={}, page={}, size={}", reporterId, page, size);

        Page<Report> pageParam = new Page<>(page, size);
        IPage<ReportVO> reportPage = reportService.getMyReports(reporterId, pageParam);

        return Result.success(reportPage);
    }
}
