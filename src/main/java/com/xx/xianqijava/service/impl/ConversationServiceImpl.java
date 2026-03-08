package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ImageMessageSendDTO;
import com.xx.xianqijava.dto.MessageSendDTO;
import com.xx.xianqijava.entity.Conversation;
import com.xx.xianqijava.entity.Message;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ConversationMapper;
import com.xx.xianqijava.mapper.MessageMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ConversationService;
import com.xx.xianqijava.service.ProductImageService;
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
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final ProductImageService productImageService;

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
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO createOrUpdateConversation(Long userId, Long targetUserId, Long relatedProductId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能与自己创建会话");
        }

        // 确保 userId1 < userId2，保持一致性
        Long smallerId = userId < targetUserId ? userId : targetUserId;
        Long largerId = userId < targetUserId ? targetUserId : userId;

        // 查询是否已存在相同（用户对 + 商品）的会话
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId1, smallerId)
                .eq(Conversation::getUserId2, largerId)
                .eq(Conversation::getConversationType, 1);

        // 如果指定了商品ID，则查找关联该商品的会话
        if (relatedProductId != null) {
            queryWrapper.eq(Conversation::getRelatedProductId, relatedProductId);
        } else {
            // 如果没有指定商品ID，查找没有关联商品的会话（普通聊天）
            queryWrapper.isNull(Conversation::getRelatedProductId)
                    .isNull(Conversation::getRelatedOrderId);
        }

        Conversation conversation = baseMapper.selectOne(queryWrapper);

        if (conversation == null) {
            // 创建新会话
            conversation = new Conversation();
            conversation.setUserId1(smallerId);
            conversation.setUserId2(largerId);
            conversation.setConversationType(1);
            conversation.setRelatedProductId(relatedProductId);
            conversation.setUnreadCountUser1(0);
            conversation.setUnreadCountUser2(0);
            conversation.setIsMutedUser1(0);
            conversation.setIsMutedUser2(0);
            conversation.setIsArchivedUser1(0);
            conversation.setIsArchivedUser2(0);
            conversation.setStatus(0);

            baseMapper.insert(conversation);
            log.info("创建新会话, conversationId={}, userId1={}, userId2={}, relatedProductId={}",
                    conversation.getConversationId(), smallerId, largerId, relatedProductId);
        } else {
            log.info("使用已有会话, conversationId={}, relatedProductId={}",
                    conversation.getConversationId(), relatedProductId);
        }

        return convertToVO(conversation, userId);
    }

    @Override
    public ConversationVO findConversationByUserAndProduct(Long userId, Long targetUserId, Long relatedProductId) {
        if (userId.equals(targetUserId)) {
            return null;
        }

        // 确保 userId1 < userId2
        Long smallerId = userId < targetUserId ? userId : targetUserId;
        Long largerId = userId < targetUserId ? targetUserId : userId;

        // 查询会话
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId1, smallerId)
                .eq(Conversation::getUserId2, largerId)
                .eq(Conversation::getConversationType, 1)
                .eq(Conversation::getStatus, 0);

        // 如果指定了商品ID，则查找关联该商品的会话
        if (relatedProductId != null) {
            queryWrapper.eq(Conversation::getRelatedProductId, relatedProductId);
        } else {
            // 如果没有指定商品ID，查找没有关联商品的会话
            queryWrapper.isNull(Conversation::getRelatedProductId)
                    .isNull(Conversation::getRelatedOrderId);
        }

        Conversation conversation = baseMapper.selectOne(queryWrapper);

        if (conversation == null) {
            return null;
        }

        return convertToVO(conversation, userId);
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
                .eq(Conversation::getStatus, 0);

        IPage<Conversation> conversationPage = baseMapper.selectPage(page, queryWrapper);

        // 在内存中排序：置顶的会话排在前面，然后按最后消息时间排序
        List<Conversation> sortedList = conversationPage.getRecords().stream()
                .sorted(Comparator
                        .comparing((Conversation c) -> {
                            // 获取置顶状态和排序
                            boolean isPinned = false;
                            Integer pinOrder = null;
                            if (c.getUserId1().equals(userId)) {
                                isPinned = c.getIsPinnedUser1() != null && c.getIsPinnedUser1() == 1;
                                pinOrder = c.getPinOrderUser1();
                            } else {
                                isPinned = c.getIsPinnedUser2() != null && c.getIsPinnedUser2() == 1;
                                pinOrder = c.getPinOrderUser2();
                            }
                            // 置顶的按pinOrder排序，未置顶的排在后面
                            return isPinned ? (pinOrder != null ? 0 - pinOrder : 0) : 1000000;
                        })
                        .thenComparing(Conversation::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .collect(Collectors.toList());

        // 更新分页结果中的记录
        conversationPage.getRecords().clear();
        conversationPage.getRecords().addAll(sortedList);

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
    public void pinConversation(Long conversationId, Long userId) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此会话");
        }

        // 获取当前最大的置顶排序值
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .eq(Conversation::getUserId1, userId)
                        .or()
                        .eq(Conversation::getUserId2, userId)
                )
                .eq(Conversation::getStatus, 0);
        List<Conversation> userConversations = baseMapper.selectList(queryWrapper);

        int maxPinOrder = 0;
        for (Conversation c : userConversations) {
            if (conversation.getUserId1().equals(userId)) {
                if (c.getPinOrderUser1() != null && c.getPinOrderUser1() > maxPinOrder) {
                    maxPinOrder = c.getPinOrderUser1();
                }
            } else {
                if (c.getPinOrderUser2() != null && c.getPinOrderUser2() > maxPinOrder) {
                    maxPinOrder = c.getPinOrderUser2();
                }
            }
        }

        // 设置置顶
        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Conversation::getConversationId, conversationId);

        if (conversation.getUserId1().equals(userId)) {
            if (conversation.getIsPinnedUser1() == null || conversation.getIsPinnedUser1() != 1) {
                updateWrapper.set(Conversation::getIsPinnedUser1, 1)
                        .set(Conversation::getPinOrderUser1, maxPinOrder + 1);
            }
        } else {
            if (conversation.getIsPinnedUser2() == null || conversation.getIsPinnedUser2() != 1) {
                updateWrapper.set(Conversation::getIsPinnedUser2, 1)
                        .set(Conversation::getPinOrderUser2, maxPinOrder + 1);
            }
        }

        baseMapper.update(null, updateWrapper);
        log.info("置顶会话成功, conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpinConversation(Long conversationId, Long userId) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此会话");
        }

        // 取消置顶
        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Conversation::getConversationId, conversationId);

        if (conversation.getUserId1().equals(userId)) {
            updateWrapper.set(Conversation::getIsPinnedUser1, 0)
                    .set(Conversation::getPinOrderUser1, null);
        } else {
            updateWrapper.set(Conversation::getIsPinnedUser2, 0)
                    .set(Conversation::getPinOrderUser2, null);
        }

        baseMapper.update(null, updateWrapper);
        log.info("取消置顶会话成功, conversationId={}, userId={}", conversationId, userId);
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
        // 根据消息类型设置友好的显示内容
        String displayContent = getMessageDisplayContent(message.getType(), message.getContent());
        conversation.setLastMessageContent(displayContent);
        conversation.setLastMessageType(message.getType());
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
                        "message", convertMessageToVO(message, toUserId)
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息不存在");
        }

        // 检查权限：只有发送者可以撤回消息
        if (!message.getFromUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能撤回自己发送的消息");
        }

        // 检查消息状态：已撤回或已删除的消息不能撤回
        if (message.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息已被撤回或删除");
        }

        // 检查时间：只能撤回2分钟内的消息
        if (message.getCreateTime() != null) {
            LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
            if (message.getCreateTime().isBefore(twoMinutesAgo)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "超过2分钟，无法撤回消息");
            }
        }

        // 撤回消息
        message.setStatus(1); // 1-撤回
        messageMapper.updateById(message);

        // 通过WebSocket通知对方消息已撤回
        Conversation conversation = baseMapper.selectById(message.getConversationId());
        Long toUserId = conversation.getUserId1().equals(userId)
                ? conversation.getUserId2() : conversation.getUserId1();

        webSocketHandler.sendMessageToUser(toUserId, "message_recalled",
                Map.of(
                        "conversationId", conversation.getConversationId(),
                        "messageId", messageId
                ));

        log.info("撤回消息成功, messageId={}, userId={}", messageId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendImageMessage(ImageMessageSendDTO sendDTO, Long userId) {
        log.info("发送图片消息, conversationId={}, imageUrl={}, userId={}",
                sendDTO.getConversationId(), sendDTO.getImageUrl(), userId);

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

        // 构建图片信息的extraData
        Map<String, Object> imageData = new java.util.HashMap<>();
        imageData.put("imageUrl", sendDTO.getImageUrl());
        if (sendDTO.getWidth() != null) {
            imageData.put("width", sendDTO.getWidth());
        }
        if (sendDTO.getHeight() != null) {
            imageData.put("height", sendDTO.getHeight());
        }
        if (sendDTO.getSize() != null) {
            imageData.put("size", sendDTO.getSize());
        }
        if (sendDTO.getThumbnailUrl() != null) {
            imageData.put("thumbnailUrl", sendDTO.getThumbnailUrl());
        }

        // 创建消息
        Message message = new Message();
        message.setConversationId(sendDTO.getConversationId());
        message.setFromUserId(userId);
        message.setToUserId(toUserId);
        message.setContent(sendDTO.getImageUrl()); // content存储图片URL
        message.setType(2); // 2-图片消息
        message.setParentMessageId(sendDTO.getParentMessageId());
        message.setIsRead(0);
        message.setSendStatus(1);
        message.setStatus(0);
        message.setExtraData(JSONUtil.toJsonStr(imageData));

        messageMapper.insert(message);

        // 更新会话的最后消息信息
        conversation.setLastMessageId(message.getMessageId());
        // 根据消息类型设置友好的显示内容
        String displayContent = getMessageDisplayContent(message.getType(), message.getContent());
        conversation.setLastMessageContent(displayContent);
        conversation.setLastMessageType(message.getType());
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
                        "message", convertMessageToVO(message, toUserId)
                ));

        log.info("发送图片消息成功, messageId={}, conversationId={}, from={}, to={}",
                message.getMessageId(), sendDTO.getConversationId(), userId, toUserId);

        return convertMessageToVO(message, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendProductCardMessage(Long conversationId, Long productId, Long userId) {
        log.info("发送商品卡片消息, conversationId={}, productId={}, userId={}",
                conversationId, productId, userId);

        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限在此会话中发送消息");
        }

        // 查询商品信息
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不存在");
        }

        // 确定接收者
        Long toUserId = conversation.getUserId1().equals(userId) ? conversation.getUserId2() : conversation.getUserId1();

        // 构建商品卡片数据
        Map<String, Object> productCardData = Map.of(
                "productId", product.getProductId(),
                "title", product.getTitle(),
                "price", product.getPrice(),
                "conditionLevel", product.getConditionLevel(),
                "location", product.getLocation(),
                "coverImage", productImageService.getCoverImage(productId)
        );

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setFromUserId(userId);
        message.setToUserId(toUserId);
        message.setContent(product.getTitle()); // content 存储商品标题
        message.setType(3); // 3-商品卡片
        message.setParentMessageId(null);
        message.setIsRead(0);
        message.setSendStatus(1);
        message.setStatus(0);
        message.setExtraData(JSONUtil.toJsonStr(productCardData));

        messageMapper.insert(message);

        // 更新会话的最后消息信息
        conversation.setLastMessageId(message.getMessageId());
        String displayContent = "[商品] " + product.getTitle();
        conversation.setLastMessageContent(displayContent);
        conversation.setLastMessageType(message.getType());
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
                        "message", convertMessageToVO(message, toUserId)
                ));

        log.info("发送商品卡片消息成功, messageId={}, conversationId={}, productId={}, from={}, to={}",
                message.getMessageId(), conversationId, productId, userId, toUserId);

        return convertMessageToVO(message, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendOrderCardMessage(Long conversationId, Long orderId, Long userId) {
        log.info("发送订单卡片消息, conversationId={}, orderId={}, userId={}",
                conversationId, orderId, userId);

        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话不存在");
        }

        // 验证用户是否参与该会话
        if (!conversation.getUserId1().equals(userId) && !conversation.getUserId2().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限在此会话中发送消息");
        }

        // 查询订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单不存在");
        }

        // 确定接收者
        Long toUserId = conversation.getUserId1().equals(userId) ? conversation.getUserId2() : conversation.getUserId1();

        // 构建订单卡片数据
        Map<String, Object> orderCardData = Map.of(
                "orderId", order.getOrderId(),
                "orderNo", order.getOrderNo(),
                "amount", order.getAmount(),
                "status", order.getStatus(),
                "productId", order.getProductId(),
                "coverImage", productImageService.getCoverImage(order.getProductId())
        );

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setFromUserId(userId);
        message.setToUserId(toUserId);
        message.setContent("订单号: " + order.getOrderNo()); // content 存储订单号
        message.setType(4); // 4-订单卡片
        message.setParentMessageId(null);
        message.setIsRead(0);
        message.setSendStatus(1);
        message.setStatus(0);
        message.setExtraData(JSONUtil.toJsonStr(orderCardData));

        messageMapper.insert(message);

        // 更新会话的最后消息信息
        conversation.setLastMessageId(message.getMessageId());
        String displayContent = "[订单] " + order.getOrderNo();
        conversation.setLastMessageContent(displayContent);
        conversation.setLastMessageType(message.getType());
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
                        "message", convertMessageToVO(message, toUserId)
                ));

        log.info("发送订单卡片消息成功, messageId={}, conversationId={}, orderId={}, from={}, to={}",
                message.getMessageId(), conversationId, orderId, userId, toUserId);

        return convertMessageToVO(message, userId);
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
            vo.setIsTop(conversation.getIsPinnedUser1() != null && conversation.getIsPinnedUser1() == 1 ? 1 : 0);
        } else {
            vo.setUnreadCount(conversation.getUnreadCountUser2());
            vo.setIsMuted(conversation.getIsMutedUser2());
            vo.setIsArchived(conversation.getIsArchivedUser2());
            vo.setIsTop(conversation.getIsPinnedUser2() != null && conversation.getIsPinnedUser2() == 1 ? 1 : 0);
        }

        // 设置时间
        if (conversation.getLastMessageTime() != null) {
            vo.setLastMessageTime(conversation.getLastMessageTime().toString());
        }

        // 转换消息类型为前端期望的字符串格式
        vo.setLastMessageType(getMessageTypeString(conversation.getLastMessageType()));

        return vo;
    }

    /**
     * 将消息类型数字转换为字符串类型
     */
    private String getMessageTypeString(Integer type) {
        if (type == null) {
            return "text";
        }
        return switch (type) {
            case 1 -> "text";
            case 2 -> "image";
            case 3 -> "product";
            case 4 -> "order";
            case 5 -> "system";
            case 6 -> "quote";
            default -> "text";
        };
    }

    /**
     * 根据消息类型获取友好的显示内容
     * 用于会话列表中显示最后一条消息
     */
    private String getMessageDisplayContent(Integer type, String content) {
        if (type == null) {
            return content;
        }
        return switch (type) {
            case 1 -> content; // 文本消息，直接返回内容
            case 2 -> "[图片]"; // 图片消息
            case 3 -> "[商品]"; // 商品卡片
            case 4 -> "[订单]"; // 订单卡片
            case 5 -> "[系统通知]"; // 系统通知
            case 6 -> "[引用消息]"; // 引用消息
            default -> content;
        };
    }

    /**
     * 清空聊天记录
     *
     * @param conversationId 会话ID
     * @param userId        当前用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearMessages(Long conversationId, Long userId) {
        log.info("清空聊天记录, conversationId={}, userId={}", conversationId, userId);

        // 1. 验证会话是否存在且用户有权操作
        Conversation conversation = this.getById(conversationId);
        if (conversation == null || conversation.getStatus() == 1) {
            log.error("会话不存在或已删除, conversationId={}", conversationId);
            throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND);
        }

        // 检查用户是否是会话参与者
        if (!conversation.getUserId1().equals(userId) &&
            !conversation.getUserId2().equals(userId)) {
            log.error("用户无权操作此会话, conversationId={}, userId={}", conversationId, userId);
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        try {
            // 2. 逻辑删除所有消息（设置status=2）
            LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Message::getConversationId, conversationId)
                        .eq(Message::getStatus, 0)  // 只删除正常状态的消息
                        .set(Message::getStatus, 2); // 设置为已删除
            int affectedRows = messageMapper.update(null, updateWrapper);
            log.info("清空聊天记录成功, conversationId={}, affectedRows={}", conversationId, affectedRows);

            // 3. 更新会话的最后消息信息
            conversation.setLastMessageId(null);
            conversation.setLastMessageContent(null);
            conversation.setLastMessageTime(null);

            // 4. 重置未读数（根据是哪个用户）
            if (conversation.getUserId1().equals(userId)) {
                conversation.setUnreadCountUser1(0);
            }
            if (conversation.getUserId2().equals(userId)) {
                conversation.setUnreadCountUser2(0);
            }

            this.updateById(conversation);
            log.info("清空聊天记录成功, conversationId={}", conversationId);

        } catch (Exception e) {
            log.error("清空聊天记录失败, conversationId={}, userId={}, error={}",
                     conversationId, userId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
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
