package com.campustrading.repository;

import com.campustrading.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId, Pageable pageable);
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);
    long countByRevieweeId(Long revieweeId);
}
