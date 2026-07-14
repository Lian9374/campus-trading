package com.campustrading.repository;

import com.campustrading.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.buyerId = :userId OR o.sellerId = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByBuyerIdAndProductIdAndStatusNot(Long buyerId, Long productId, Order.Status status);

    long countByStatus(Order.Status status);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") Order.Status status);

    Page<Order> findByStatusAndCreatedAtBefore(Order.Status status, LocalDateTime before, Pageable pageable);
}
