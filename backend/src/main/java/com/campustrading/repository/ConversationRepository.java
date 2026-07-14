package com.campustrading.repository;

import com.campustrading.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByBuyerIdAndSellerIdAndProductId(Long buyerId, Long sellerId, Long productId);

    @Query("SELECT c FROM Conversation c WHERE c.buyerId = :userId OR c.sellerId = :userId ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
