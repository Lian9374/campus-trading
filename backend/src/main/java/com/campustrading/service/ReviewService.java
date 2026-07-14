package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.ReviewRequest;
import com.campustrading.dto.ReviewResponse;
import com.campustrading.entity.Notification;
import com.campustrading.entity.Order;
import com.campustrading.entity.Review;
import com.campustrading.entity.User;
import com.campustrading.repository.OrderRepository;
import com.campustrading.repository.ReviewRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReviewResponse createReview(Long reviewerId, ReviewRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 仅已完成订单可评价
        if (order.getStatus() != Order.Status.COMPLETED) {
            throw new BusinessException("只能评价已完成的订单");
        }

        // 评价者必须是订单的买家或卖家
        Long revieweeId;
        if (order.getBuyerId().equals(reviewerId)) {
            revieweeId = order.getSellerId();
        } else if (order.getSellerId().equals(reviewerId)) {
            revieweeId = order.getBuyerId();
        } else {
            throw new BusinessException(403, "无权评价此订单");
        }

        // 同一人对同一订单只能评价一次
        if (reviewRepository.existsByOrderIdAndReviewerId(request.getOrderId(), reviewerId)) {
            throw new BusinessException("您已评价过该订单");
        }

        // 保存评价
        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setProductId(order.getProductId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review = reviewRepository.save(review);

        // 更新被评价用户的评分
        updateUserRating(revieweeId);

        // 发送通知
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        String reviewerName = reviewer != null ? reviewer.getNickname() : "用户";
        notificationService.send(revieweeId,
                Notification.Type.REVIEW,
                "收到新评价",
                reviewerName + " 给您留下了 " + request.getRating() + " 星评价" +
                        (request.getContent() != null && !request.getContent().isBlank()
                                ? "：「" + request.getContent() + "」" : ""),
                review.getId());

        return enrichReviewResponse(review);
    }

    public Page<ReviewResponse> getUserReviews(Long userId, int page, int size) {
        page = Math.max(0, page);
        size = Math.min(50, Math.max(1, size));

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId, pageable);

        List<ReviewResponse> enriched = reviewPage.getContent().stream()
                .map(this::enrichReviewResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(enriched, pageable, reviewPage.getTotalElements());
    }

    private void updateUserRating(Long userId) {
        long count = reviewRepository.countByRevieweeId(userId);
        // 计算平均分
        Page<Review> page = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId,
                PageRequest.of(0, Math.toIntExact(count)));
        double avg = page.getContent().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        userRepository.findById(userId).ifPresent(user -> {
            user.setRatingAvg(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            user.setRatingCount((int) count);
            userRepository.save(user);
        });
    }

    private ReviewResponse enrichReviewResponse(Review review) {
        ReviewResponse resp = ReviewResponse.fromEntity(review);
        userRepository.findById(review.getReviewerId())
                .ifPresent(u -> resp.setReviewerName(u.getNickname()));
        return resp;
    }
}
