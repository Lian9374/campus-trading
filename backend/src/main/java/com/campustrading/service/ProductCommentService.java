package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.CommentRequest;
import com.campustrading.dto.CommentResponse;
import com.campustrading.entity.Notification;
import com.campustrading.entity.Product;
import com.campustrading.entity.ProductComment;
import com.campustrading.entity.User;
import com.campustrading.repository.ProductCommentRepository;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCommentService {

    private final ProductCommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse addComment(Long userId, CommentRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        ProductComment comment = new ProductComment();
        comment.setProductId(request.getProductId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        User commenter = userRepository.findById(userId).orElse(null);
        String commenterName = commenter != null ? commenter.getNickname() : "用户";

        // 通知：如果是回复 → 通知原评论者；如果是顶级留言 → 通知卖家
        if (request.getParentId() != null) {
            ProductComment parent = commentRepository.findById(request.getParentId()).orElse(null);
            if (parent != null && !parent.getUserId().equals(userId)) {
                notificationService.send(parent.getUserId(),
                        Notification.Type.COMMENT,
                        "新回复",
                        commenterName + " 回复了您在「" + product.getTitle() + "」的留言",
                        comment.getId());
            }
        } else {
            // 通知卖家
            if (!product.getSellerId().equals(userId)) {
                notificationService.send(product.getSellerId(),
                        Notification.Type.COMMENT,
                        "商品新问答",
                        commenterName + " 对您的商品「" + product.getTitle() + "」提出了一个问题",
                        comment.getId());
            }
        }

        CommentResponse resp = CommentResponse.fromEntity(comment);
        if (commenter != null) {
            resp.setUserName(commenter.getNickname());
            resp.setUserAvatar(commenter.getAvatar());
        }
        return resp;
    }

    public Page<CommentResponse> getComments(Long productId, int page, int size) {
        page = Math.max(0, page);
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductComment> commentPage = commentRepository
                .findByProductIdAndParentIdIsNullOrderByCreatedAtDesc(productId, pageable);

        List<ProductComment> comments = commentPage.getContent();
        if (comments.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 批量加载用户信息
        List<Long> userIds = comments.stream()
                .map(ProductComment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量加载回复
        List<Long> commentIds = comments.stream()
                .map(ProductComment::getId)
                .collect(Collectors.toList());
        Map<Long, List<ProductComment>> repliesMap = commentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> commentRepository.findByParentIdOrderByCreatedAtAsc(id)
                ));

        // 收集回复的 userIds
        repliesMap.values().stream()
                .flatMap(List::stream)
                .map(ProductComment::getUserId)
                .distinct()
                .forEach(uid -> {
                    if (!userMap.containsKey(uid)) {
                        userRepository.findById(uid).ifPresent(u -> userMap.put(uid, u));
                    }
                });

        List<CommentResponse> list = comments.stream().map(c -> {
            CommentResponse r = CommentResponse.fromEntity(c);
            User user = userMap.get(c.getUserId());
            if (user != null) {
                r.setUserName(user.getNickname());
                r.setUserAvatar(user.getAvatar());
            }

            List<ProductComment> replies = repliesMap.getOrDefault(c.getId(), List.of());
            r.setReplyCount(replies.size());
            // 最多展示 3 条回复
            List<CommentResponse> replyList = replies.stream().limit(3).map(rep -> {
                CommentResponse rr = CommentResponse.fromEntity(rep);
                User ru = userMap.get(rep.getUserId());
                if (ru != null) {
                    rr.setUserName(ru.getNickname());
                    rr.setUserAvatar(ru.getAvatar());
                }
                return rr;
            }).collect(Collectors.toList());
            r.setReplies(replyList);

            return r;
        }).collect(Collectors.toList());

        return new PageImpl<>(list, pageable, commentPage.getTotalElements());
    }
}
