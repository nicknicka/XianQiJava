package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.QuickReplyDTO;
import com.xx.xianqijava.entity.QuickReply;
import com.xx.xianqijava.service.QuickReplyService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.QuickReplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 快捷回复控制器
 */
@Slf4j
@Tag(name = "快捷回复管理")
@RestController
@RequestMapping("/quick-reply")
@RequiredArgsConstructor
public class QuickReplyController {

    private final QuickReplyService quickReplyService;

    /**
     * 创建快捷回复
     */
    @PostMapping
    @Operation(summary = "创建快捷回复")
    public Result<QuickReplyVO> createQuickReply(@Valid @RequestBody QuickReplyDTO dto) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建快捷回复, userId={}, title={}", userId, dto.getTitle());
        QuickReplyVO quickReplyVO = quickReplyService.createQuickReply(dto, userId);
        return Result.success("快捷回复创建成功", quickReplyVO);
    }

    /**
     * 更新快捷回复
     */
    @PutMapping("/{replyId}")
    @Operation(summary = "更新快捷回复")
    public Result<Void> updateQuickReply(
            @Parameter(description = "快捷回复ID") @PathVariable("replyId") Long replyId,
            @Valid @RequestBody QuickReplyDTO dto) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新快捷回复, replyId={}, userId={}", replyId, userId);
        quickReplyService.updateQuickReply(replyId, dto, userId);
        return Result.success("快捷回复更新成功");
    }

    /**
     * 删除快捷回复
     */
    @DeleteMapping("/{replyId}")
    @Operation(summary = "删除快捷回复")
    public Result<Void> deleteQuickReply(
            @Parameter(description = "快捷回复ID") @PathVariable("replyId") Long replyId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除快捷回复, replyId={}, userId={}", replyId, userId);
        quickReplyService.deleteQuickReply(replyId, userId);
        return Result.success("快捷回复删除成功");
    }

    /**
     * 获取快捷回复列表
     */
    @GetMapping
    @Operation(summary = "获取快捷回复列表")
    public Result<IPage<QuickReplyVO>> getQuickReplyList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询快捷回复列表, userId={}, page={}, size={}", userId, page, size);

        Page<QuickReply> pageParam = new Page<>(page, size);
        IPage<QuickReplyVO> quickReplyPage = quickReplyService.getQuickReplyList(userId, pageParam);

        return Result.success(quickReplyPage);
    }

    /**
     * 获取系统预设快捷回复
     */
    @GetMapping("/system")
    @Operation(summary = "获取系统预设快捷回复")
    public Result<List<QuickReplyVO>> getSystemQuickReplies() {
        log.info("查询系统预设快捷回复");
        List<QuickReplyVO> quickReplies = quickReplyService.getSystemQuickReplies();
        return Result.success(quickReplies);
    }
}
