package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.MessageSendDTO;
import com.xx.xianqijava.entity.Conversation;
import com.xx.xianqijava.entity.Message;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ConversationMapper;
import com.xx.xianqijava.mapper.MessageMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ConversationService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ConversationVO;
import com.xx.xianqijava.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Autowired
    private com.xx.xianqijava.websocket.WebSocketHandler webSocketHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long getOrCreateOneToOneConversation(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能与自己创建会话");
        }

        // 确保 userId1 < userId2，便于查询
        Long smallerId = userId1 < userId2 ? userId1 : userId2;
        Long largerId = userId1 < userId2 ? userId2 : userId1;

        // 查询是否已存在会话
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId1, smallerId)
                .eq(Conversation::getUserId2, largerId)
                .eq(Conversation::getConversationType, 1);
        Conversation conversation = baseMapper.selectOne(queryWrapper);

        if (conversation == null) {
            // 创建新会话
            conversation = new Conversation();
            conversation.setUserId1(smallerId);
            conversation.setUserId2(largerId);
            conversation.setConversationType(1);
            conversation.setUnreadCountUser1(0);
            conversation.setUnreadCountUser2(0);
            conversation.setIsMutedUser1(0);
            conversation.setIsMutedUser2(0);
            conversation.setIsArchivedUser1(0);
            conversation.setIsArchivedUser2(0);
            conversation.setStatus(0);

            baseMapper.insert(conversation);
            log.info("创建新会话, conversationId={}, userId1={}, userId2={}",
                    conversation.getConversationId(), userId1, userId2);
        }

        return conversation.getConversationId();
    }

    @Override
    public IPage<ConversationVO> getConversationList(Long userId, Page<Conversation> page) {
        // 查询用户参与的所有会话
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .eq(Conversation::getUserId1, userId)
                        .or()
                        .eq(Conversation::getUserId2, userId)
                )
                .eq(Conversation::getStatus, 0)
                .orderByDesc(Conversation::getLastMessageTime);

        IPage<Conversation> conversationPage = baseMapper.selectPage(page, queryWrapper);

        return conversationPage.convert(conversation -> convertToVO(conversation, userId));
    }

    @Override
    public ConversationVO getConversationDetail(Long conversationId, Long userId) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限访问此会话");
        }

        return convertToVO(conversation, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限删除此会话");
        }

        // 标记为已删除
        conversation.setStatus(1);
        baseMapper.updateById(conversation);

        log.info("删除会话成功, conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConversationAsRead(Long conversationId, Long userId) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此会话");
        }

        // 清空未读数
        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        if (conversation.getUserId1().equals(userId)) {
            updateWrapper.set(Conversation::getUnreadCountUser1, 0);
        } else {
            updateWrapper.set(Conversation::getUnreadCountUser2, 0);
        }
        updateWrapper.eq(Conversation::getConversationId, conversationId);
        baseMapper.update(null, updateWrapper);

        // 标记所有未读消息为已读
        LambdaUpdateWrapper<Message> messageUpdateWrapper = new LambdaUpdateWrapper<>();
        messageUpdateWrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1)
                .set(Message::getReadTime, LocalDateTime.now());
        messageMapper.update(null, messageUpdateWrapper);

        log.info("标记会话已读, conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(MessageSendDTO sendDTO, Long userId) {
        Conversation conversation = baseMapper.selectById(sendDTO.getConversationId());
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限在此会话中发送消息");
        }

        // 确定接收者
        Long toUserId = conversation.getUserId1().equals(userId) ? conversation.getUserId2() : conversation.getUserId1();

        // 创建消息
        Message message = new Message();
        message.setConversationId(sendDTO.getConversationId());
        message.setFromUserId(userId);
        message.setToUserId(toUserId);
        message.setContent(sendDTO.getContent());
        message.setType(sendDTO.getType());
        message.setParentMessageId(sendDTO.getParentMessageId());
        message.setIsRead(0);
        message.setSendStatus(1); // 默认发送成功
        message.setStatus(0);

        messageMapper.insert(message);

        // 更新会话的最后消息信息
        conversation.setLastMessageId(message.getMessageId());
        conversation.setLastMessageContent(message.getContent());
        conversation.setLastMessageTime(message.getCreateTime());

        // 增加接收者的未读数
        if (conversation.getUserId1().equals(toUserId)) {
            conversation.setUnreadCountUser1(conversation.getUnreadCountUser1() + 1);
        } else {
            conversation.setUnreadCountUser2(conversation.getUnreadCountUser2() + 1);
        }

        baseMapper.updateById(conversation);

        // 通过WebSocket发送消息给接收者
        webSocketHandler.sendMessageToUser(toUserId, "new_message",
                Map.of(
                        "conversationId", conversation.getConversationId(),
                        "message", convertMessageToVO(message, userId)
                ));

        log.info("发送消息成功, messageId={}, conversationId={}, from={}, to={}",
                message.getMessageId(), sendDTO.getConversationId(), userId, toUserId);

        return convertMessageToVO(message, userId);
    }

    @Override
    public IPage<MessageVO> getMessages(Long conversationId, Long userId, Page<Message> page) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看此会话的消息");
        }

        // 查询消息列表
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getStatus, 0) // 未删除的消息
                .orderByDesc(Message::getCreateTime);

        IPage<Message> messagePage = messageMapper.selectPage(page, queryWrapper);

        return messagePage.convert(msg -> convertMessageToVO(msg, userId));
    }

    /**
     * 转换为VO
     */
    private ConversationVO convertToVO(Conversation conversation, Long currentUserId) {
        ConversationVO vo = new ConversationVO();
        BeanUtil.copyProperties(conversation, vo);

        // 确定对方用户ID
        Long otherUserId = conversation.getUserId1().equals(currentUserId)
                ? conversation.getUserId2() : conversation.getUserId1();
        vo.setOtherUserId(otherUserId);

        // 获取对方用户信息
        User otherUser = userMapper.selectById(otherUserId);
        if (otherUser != null) {
            vo.setOtherUserNickname(otherUser.getNickname());
            vo.setOtherUserAvatar(otherUser.getAvatar());
        }

        // 设置对方在线状态
        vo.setOtherUserOnline(webSocketHandler.isUserOnline(otherUserId));

        // 设置未读数
        if (conversation.getUserId1().equals(currentUserId)) {
            vo.setUnreadCount(conversation.getUnreadCountUser1());
            vo.setIsMuted(conversation.getIsMutedUser1());
            vo.setIsArchived(conversation.getIsArchivedUser1());
        } else {
            vo.setUnreadCount(conversation.getUnreadCountUser2());
            vo.setIsMuted(conversation.getIsMutedUser2());
            vo.setIsArchived(conversation.getIsArchivedUser2());
        }

        // 设置时间
        if (conversation.getLastMessageTime() != null) {
            vo.setLastMessageTime(conversation.getLastMessageTime().toString());
        }

        return vo;
    }

    /**
     * 转换消息为VO
     */
    private MessageVO convertMessageToVO(Message message, Long currentUserId) {
        MessageVO vo = new MessageVO();
        BeanUtil.copyProperties(message, vo);

        // 获取发送者信息
        User fromUser = userMapper.selectById(message.getFromUserId());
        if (fromUser != null) {
            vo.setFromUserNickname(fromUser.getNickname());
            vo.setFromUserAvatar(fromUser.getAvatar());
        }

        // 设置是否为当前用户发送的消息
        vo.setMine(message.getFromUserId().equals(currentUserId));

        // 设置创建时间
        if (message.getCreateTime() != null) {
            vo.setCreateTime(message.getCreateTime().toString());
        }

        // 如果有引用消息，获取被引用消息的内容
        if (message.getParentMessageId() != null) {
            Message parentMessage = messageMapper.selectById(message.getParentMessageId());
            if (parentMessage != null) {
                vo.setParentMessageContent(parentMessage.getContent());
            }
        }

        return vo;
    }
}
