package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.ImageMessageSendDTO;
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
     * 创建或获取基于商品的会话
     * 支持同一对用户基于不同商品创建多个独立会话
     *
     * @param userId         当前用户ID
     * @param targetUserId   对方用户ID
     * @param relatedProductId 关联的商品ID（可选）
     * @return 会话ID
     */
    Long createOrUpdateConversation(Long userId, Long targetUserId, Long relatedProductId);

    /**
     * 根据用户ID和商品ID查找会话
     *
     * @param userId         当前用户ID
     * @param targetUserId   对方用户ID
     * @param relatedProductId 关联的商品ID（可选）
     * @return 会话VO，如果不存在返回null
     */
    ConversationVO findConversationByUserAndProduct(Long userId, Long targetUserId, Long relatedProductId);

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
     * 置顶会话
     *
     * @param conversationId 会话ID
     * @param userId        当前用户ID
     */
    void pinConversation(Long conversationId, Long userId);

    /**
     * 取消置顶会话
     *
     * @param conversationId 会话ID
     * @param userId        当前用户ID
     */
    void unpinConversation(Long conversationId, Long userId);

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

    /**
     * 发送图片消息
     *
     * @param sendDTO 图片消息DTO
     * @param userId  当前用户ID
     * @return 消息VO
     */
    MessageVO sendImageMessage(ImageMessageSendDTO sendDTO, Long userId);
}
