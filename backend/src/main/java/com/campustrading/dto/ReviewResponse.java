package com.campustrading.dto;

import com.campustrading.entity.Review;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerName;
    private Long productId;
    private String productTitle;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse resp = new ReviewResponse();
        resp.setId(review.getId());
        resp.setOrderId(review.getOrderId());
        resp.setReviewerId(review.getReviewerId());
        resp.setProductId(review.getProductId());
        resp.setRating(review.getRating());
        resp.setContent(review.getContent());
        resp.setCreatedAt(review.getCreatedAt());
        return resp;
    }
}
