package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.UserFeedbackDTO;
import com.xx.xianqijava.entity.UserFeedback;
import com.xx.xianqijava.service.UserFeedbackService;
import com.xx.xianqijava.vo.UserFeedbackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户反馈管理控制器 - 管理端
 */
@Slf4j
@Tag(name = "用户反馈管理")
@RestController
@RequestMapping("/admin/feedback")
@RequiredArgsConstructor
public class FeedbackManageController {

    private final UserFeedbackService userFeedbackService;

    /**
     * 获取反馈列表（管理员）
     */
    @GetMapping("/list")
    @Operation(summary = "获取反馈列表（管理员）")
    public Result<IPage<UserFeedbackVO>> getFeedbackList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "反馈类型") @RequestParam(required = false) String type,
            @Parameter(description = "处理状态") @RequestParam(required = false) Integer status) {
        log.info("查询反馈列表, page={}, size={}, type={}, status={}", page, size, type, status);

        Page<UserFeedback> pageParam = new Page<>(page, size);
        IPage<UserFeedbackVO> feedbackPage = userFeedbackService.getFeedbackList(pageParam, type, status);

        return Result.success(feedbackPage);
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取反馈详情")
    public Result<UserFeedbackVO> getFeedbackDetail(
            @Parameter(description = "反馈ID") @PathVariable("id") Long id) {
        log.info("查询反馈详情, id={}", id);
        UserFeedbackVO feedbackVO = userFeedbackService.getFeedbackDetail(id);
        return Result.success(feedbackVO);
    }

    /**
     * 处理反馈
     */
    @PutMapping("/handle")
    @Operation(summary = "处理反馈")
    public Result<Void> handleFeedback(
            @Parameter(description = "反馈ID") @RequestParam Long id,
            @Parameter(description = "处理结果") @RequestParam String result) {
        log.info("处理反馈, id={}, result={}", id, result);
        userFeedbackService.handleFeedback(id, result);
        return Result.success("反馈处理成功");
    }

    /**
     * 删除反馈
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除反馈")
    public Result<Void> deleteFeedback(
            @Parameter(description = "反馈ID") @PathVariable("id") Long id) {
        log.info("删除反馈, id={}", id);
        userFeedbackService.deleteFeedback(id);
        return Result.success("反馈删除成功");
    }

    /**
     * 获取反馈统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取反馈统计数据")
    public Result<Map<String, Object>> getStatistics() {
        log.info("查询反馈统计数据");
        Map<String, Object> statistics = userFeedbackService.getStatistics();
        return Result.success(statistics);
    }
}
