package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.MessageSendDTO;
import com.xx.xianqijava.entity.Conversation;
import com.xx.xianqijava.vo.ConversationVO;
import com.xx.xianqijava.vo.MessageVO;

/**
 * 会话服务接口
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 获取或创建单聊会话
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话ID
     */
    Long getOrCreateOneToOneConversation(Long userId1, Long userId2);

    /**
     * 获取会话列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 会话列表
     */
    IPage<ConversationVO> getConversationList(Long userId, Page<Conversation> page);

    /**
     * 获取会话详情
     *
     * @param conversationId 会话ID
     * @param userId        当前用户ID
     * @return 会话详情
     */
    ConversationVO getConversationDetail(Long conversationId, Long userId);

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @param userId        当前用户ID
     */
    void deleteConversation(Long conversationId, Long userId);

    /**
     * 标记会话为已读
     *
     * @param conversationId 会话ID
     * @param userId        当前用户ID
     */
    void markConversationAsRead(Long conversationId, Long userId);

    /**
     * 发送消息
     *
     * @param sendDTO 发送消息DTO
     * @param userId  当前用户ID
     * @return 消息VO
     */
    MessageVO sendMessage(MessageSendDTO sendDTO, Long userId);

    /**
     * 获取历史消息
     *
     * @param conversationId 会话ID
     * @param userId        当前用户ID
     * @param page          分页参数
     * @return 消息列表
     */
    IPage<MessageVO> getMessages(Long conversationId, Long userId, Page<com.xx.xianqijava.entity.Message> page);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId    当前用户ID
     */
    void recallMessage(Long messageId, Long userId);
}
