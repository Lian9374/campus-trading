package com.campustrading.dto;

import com.campustrading.entity.Conversation;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationResponse {

    private Long id;
    private Long productId;
    private String productTitle;
    private String productCover;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;

    public static ConversationResponse fromEntity(Conversation c) {
        ConversationResponse r = new ConversationResponse();
        r.setId(c.getId());
        r.setProductId(c.getProductId());
        r.setLastMessage(c.getLastMessage());
        r.setLastMessageAt(c.getLastMessageAt());
        return r;
    }
}
