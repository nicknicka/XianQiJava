package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.UserFeedbackDTO;
import com.xx.xianqijava.entity.UserFeedback;
import com.xx.xianqijava.service.UserFeedbackService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.UserFeedbackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户反馈控制器
 */
@Slf4j
@Tag(name = "用户反馈管理")
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class UserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    /**
     * 创建用户反馈
     */
    @PostMapping
    @Operation(summary = "创建用户反馈")
    public Result<UserFeedbackVO> createUserFeedback(@Valid @RequestBody UserFeedbackDTO dto) {
        Long userId = null;
        try {
            userId = SecurityUtil.getCurrentUserIdRequired();
        } catch (Exception e) {
            // 允许匿名反馈
            log.info("匿名用户反馈");
        }

        log.info("创建用户反馈, userId={}, type={}", userId, dto.getType());
        UserFeedbackVO feedbackVO = userFeedbackService.createUserFeedback(dto, userId);
        return Result.success("感谢您的反馈，我们会尽快处理", feedbackVO);
    }

    /**
     * 获取我的反馈列表
     */
    @GetMapping
    @Operation(summary = "获取我的反馈列表")
    public Result<IPage<UserFeedbackVO>> getMyFeedback(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的反馈列表, userId={}, page={}, size={}", userId, page, size);

        Page<UserFeedback> pageParam = new Page<>(page, size);
        IPage<UserFeedbackVO> feedbackPage = userFeedbackService.getMyFeedback(userId, pageParam);

        return Result.success(feedbackPage);
    }
}
