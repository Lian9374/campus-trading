package com.campustrading.repository;

import com.campustrading.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    long countByConversationIdAndSenderIdNotAndIsReadFalse(Long conversationId, Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId IN :ids AND m.senderId <> :userId AND m.isRead = false")
    long countUnreadByConversationIds(@Param("ids") List<Long> conversationIds, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.senderId <> :userId AND m.isRead = false")
    int markAsReadByConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
