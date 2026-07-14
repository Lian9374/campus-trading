package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.ConversationResponse;
import com.campustrading.dto.MessageResponse;
import com.campustrading.dto.SendMessageRequest;
import com.campustrading.entity.Conversation;
import com.campustrading.entity.Message;
import com.campustrading.entity.Notification;
import com.campustrading.entity.Product;
import com.campustrading.entity.User;
import com.campustrading.repository.ConversationRepository;
import com.campustrading.repository.FollowRepository;
import com.campustrading.repository.MessageRepository;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final FollowRepository followRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ConversationResponse sendMessage(Long senderId, SendMessageRequest request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException("不能给自己发消息");
        }

        Long buyerId, sellerId;
        Long productId;
        String productTitle = "私信对话";

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new BusinessException("商品不存在"));
            productId = product.getId();
            productTitle = product.getTitle();

            // 判断买卖双方身份
            if (product.getSellerId().equals(senderId)) {
                sellerId = senderId;
                buyerId = request.getReceiverId();
            } else {
                buyerId = senderId;
                sellerId = request.getReceiverId();
            }
        } else {
            // 无商品的直接私信：按ID大小归一化，保证双向查找一致
            if (senderId < request.getReceiverId()) {
                buyerId = senderId;
                sellerId = request.getReceiverId();
            } else {
                buyerId = request.getReceiverId();
                sellerId = senderId;
            }
            productId = 0L; // 0表示非商品私信
        }

        // 查找或创建会话
        Long finalProductId = productId;
        Conversation conversation = conversationRepository
                .findByBuyerIdAndSellerIdAndProductId(buyerId, sellerId, finalProductId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setBuyerId(buyerId);
                    c.setSellerId(sellerId);
                    c.setProductId(finalProductId);
                    return conversationRepository.save(c);
                });

        // 检查消息限制：单方面只能发3条，互相关注后才能无限聊天
        long senderMsgCount = messageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversation.getId(), PageRequest.of(0, 1000))
                .getContent().stream()
                .filter(m -> m.getSenderId().equals(senderId))
                .count();
        if (senderMsgCount >= 3) {
            Long receiverId = senderId.equals(buyerId) ? sellerId : buyerId;
            boolean senderFollowsReceiver = followRepository.existsByFollowerIdAndFollowingId(senderId, receiverId);
            boolean receiverFollowsSender = followRepository.existsByFollowerIdAndFollowingId(receiverId, senderId);
            if (!(senderFollowsReceiver && receiverFollowsSender)) {
                throw new BusinessException("已发送3条消息，需要互相关注后才能继续聊天。请先关注对方！");
            }
        }

        // 更新最后消息
        String preview = request.getContent().length() > 100
                ? request.getContent().substring(0, 100) + "..."
                : request.getContent();
        conversation.setLastMessage(preview);
        conversation.setLastMessageAt(java.time.LocalDateTime.now());
        conversationRepository.save(conversation);

        // 保存消息
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        messageRepository.save(message);

        // 通知接收者
        User sender = userRepository.findById(senderId).orElse(null);
        String senderName = sender != null ? sender.getNickname() : "用户";
        notificationService.send(request.getReceiverId(),
                Notification.Type.MESSAGE,
                "新私信",
                senderName + " 给您发了一条私信" + (request.getProductId() != null ? "（关于「" + productTitle + "」）" : ""),
                conversation.getId());

        return enrichConversation(conversation, senderId);
    }

    public Page<ConversationResponse> getConversationList(Long userId, int page, int size) {
        page = Math.max(0, page);
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size);

        Page<Conversation> conversationPage = conversationRepository.findByUserId(userId, pageable);
        List<Conversation> conversations = conversationPage.getContent();

        if (conversations.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 批量加载商品信息（排除 productId=0 的非商品私信）
        List<Long> productIds = conversations.stream()
                .map(Conversation::getProductId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of()
                : productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 批量加载"对方"用户信息
        List<Long> otherUserIds = conversations.stream()
                .map(c -> c.getBuyerId().equals(userId) ? c.getSellerId() : c.getBuyerId())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(otherUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量计算未读数
        List<Long> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());
        Map<Long, Long> unreadMap = conversations.stream()
                .collect(Collectors.toMap(
                        Conversation::getId,
                        c -> messageRepository.countByConversationIdAndSenderIdNotAndIsReadFalse(c.getId(), userId)
                ));

        List<ConversationResponse> list = conversations.stream().map(c -> {
            ConversationResponse r = ConversationResponse.fromEntity(c);

            if (c.getProductId() != null && c.getProductId() > 0) {
                Product product = productMap.get(c.getProductId());
                if (product != null) {
                    r.setProductTitle(product.getTitle());
                    r.setProductCover(product.getCoverImage());
                }
            } else {
                r.setProductTitle("私信对话");
            }

            Long otherUserId = c.getBuyerId().equals(userId) ? c.getSellerId() : c.getBuyerId();
            r.setOtherUserId(otherUserId);
            User otherUser = userMap.get(otherUserId);
            if (otherUser != null) {
                r.setOtherUserName(otherUser.getNickname());
                r.setOtherUserAvatar(otherUser.getAvatar());
            }

            r.setUnreadCount(unreadMap.getOrDefault(c.getId(), 0L));
            return r;
        }).collect(Collectors.toList());

        return new PageImpl<>(list, pageable, conversationPage.getTotalElements());
    }

    @Transactional
    public Page<MessageResponse> getMessages(Long userId, Long conversationId, int page, int size) {
        page = Math.max(0, page);
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        // 验证参与者身份
        if (!conversation.getBuyerId().equals(userId) && !conversation.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权查看该会话");
        }

        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        // 批量加载发送者信息
        List<Long> senderIds = messagePage.getContent().stream()
                .map(Message::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<MessageResponse> list = messagePage.getContent().stream().map(m -> {
            MessageResponse r = MessageResponse.fromEntity(m);
            User sender = userMap.get(m.getSenderId());
            if (sender != null) {
                r.setSenderName(sender.getNickname());
                r.setSenderAvatar(sender.getAvatar());
            }
            return r;
        }).collect(Collectors.toList());

        // 标记对方消息为已读
        markConversationRead(userId, conversationId);

        return new PageImpl<>(list, pageable, messagePage.getTotalElements());
    }

    @Transactional
    public void markConversationRead(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        if (!conversation.getBuyerId().equals(userId) && !conversation.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权操作该会话");
        }

        messageRepository.markAsReadByConversation(conversationId, userId);
    }

    public long getTotalUnreadCount(Long userId) {
        // 获取用户所有会话ID
        Page<Conversation> conversations = conversationRepository.findByUserId(userId,
                PageRequest.of(0, 1000));
        if (conversations.isEmpty()) return 0;

        List<Long> ids = conversations.getContent().stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());

        return messageRepository.countUnreadByConversationIds(ids, userId);
    }

    private ConversationResponse enrichConversation(Conversation conversation, Long senderId) {
        ConversationResponse r = ConversationResponse.fromEntity(conversation);

        if (conversation.getProductId() != null && conversation.getProductId() > 0) {
            Product product = productRepository.findById(conversation.getProductId()).orElse(null);
            if (product != null) {
                r.setProductTitle(product.getTitle());
                r.setProductCover(product.getCoverImage());
            }
        } else {
            r.setProductTitle("私信对话");
        }

        // 对方 = 非发送者
        Long otherUserId = conversation.getBuyerId().equals(senderId)
                ? conversation.getSellerId() : conversation.getBuyerId();
        r.setOtherUserId(otherUserId);

        User otherUser = userRepository.findById(otherUserId).orElse(null);
        if (otherUser != null) {
            r.setOtherUserName(otherUser.getNickname());
            r.setOtherUserAvatar(otherUser.getAvatar());
        }

        return r;
    }
}
