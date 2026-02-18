package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.ImageMessageSendDTO;
import com.xx.xianqijava.dto.MessageSendDTO;
import com.xx.xianqijava.entity.Conversation;
import com.xx.xianqijava.entity.Message;
import com.xx.xianqijava.service.ConversationService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ConversationVO;
import com.xx.xianqijava.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 会话控制器
 */
@Slf4j
@Tag(name = "会话管理")
@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 获取或创建单聊会话
     */
    @Operation(summary = "获取或创建单聊会话")
    @PostMapping("/one-to-one")
    public Result<Long> getOrCreateOneToOneConversation(
            @Parameter(description = "对方用户ID") @RequestParam("userId") Long otherUserId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取或创建单聊会话, userId={}, otherUserId={}", userId, otherUserId);
        Long conversationId = conversationService.getOrCreateOneToOneConversation(userId, otherUserId);
        return Result.success(conversationId);
    }

    /**
     * 获取会话列表
     */
    @Operation(summary = "获取会话列表")
    @GetMapping
    public Result<IPage<ConversationVO>> getConversationList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询会话列表, userId={}, page={}, size={}", userId, page, size);

        Page<Conversation> pageParam = new Page<>(page, size);
        IPage<ConversationVO> conversationPage = conversationService.getConversationList(userId, pageParam);

        return Result.success(conversationPage);
    }

    /**
     * 获取会话详情
     */
    @Operation(summary = "获取会话详情")
    @GetMapping("/{id}")
    public Result<ConversationVO> getConversationDetail(
            @Parameter(description = "会话ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询会话详情, conversationId={}, userId={}", id, userId);
        ConversationVO conversationVO = conversationService.getConversationDetail(id, userId);
        return Result.success(conversationVO);
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConversation(
            @Parameter(description = "会话ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除会话, conversationId={}, userId={}", id, userId);
        conversationService.deleteConversation(id, userId);
        return Result.success("会话删除成功");
    }

    /**
     * 标记会话为已读
     */
    @Operation(summary = "标记会话为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markConversationAsRead(
            @Parameter(description = "会话ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("标记会话已读, conversationId={}, userId={}", id, userId);
        conversationService.markConversationAsRead(id, userId);
        return Result.success("标记成功");
    }

    /**
     * 发送消息
     */
    @Operation(summary = "发送消息")
    @PostMapping("/message")
    public Result<MessageVO> sendMessage(@Valid @RequestBody MessageSendDTO sendDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("发送消息, userId={}, conversationId={}", userId, sendDTO.getConversationId());
        MessageVO messageVO = conversationService.sendMessage(sendDTO, userId);
        return Result.success("消息发送成功", messageVO);
    }

    /**
     * 获取历史消息
     */
    @Operation(summary = "获取历史消息")
    @GetMapping("/{id}/messages")
    public Result<IPage<MessageVO>> getMessages(
            @Parameter(description = "会话ID") @PathVariable("id") Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询历史消息, conversationId={}, userId={}, page={}, size={}", id, userId, page, size);

        Page<Message> pageParam = new Page<>(page, size);
        IPage<MessageVO> messagePage = conversationService.getMessages(id, userId, pageParam);

        return Result.success(messagePage);
    }

    /**
     * 撤回消息
     */
    @Operation(summary = "撤回消息")
    @PutMapping("/message/{messageId}/recall")
    public Result<Void> recallMessage(
            @Parameter(description = "消息ID") @PathVariable("messageId") Long messageId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("撤回消息, messageId={}, userId={}", messageId, userId);
        conversationService.recallMessage(messageId, userId);
        return Result.success("消息撤回成功");
    }

    /**
     * 发送图片消息
     */
    @Operation(summary = "发送图片消息")
    @PostMapping("/message/image")
    public Result<MessageVO> sendImageMessage(@Valid @RequestBody ImageMessageSendDTO sendDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("发送图片消息, userId={}, conversationId={}, imageUrl={}",
                userId, sendDTO.getConversationId(), sendDTO.getImageUrl());
        MessageVO messageVO = conversationService.sendImageMessage(sendDTO, userId);
        return Result.success("图片消息发送成功", messageVO);
    }
}
