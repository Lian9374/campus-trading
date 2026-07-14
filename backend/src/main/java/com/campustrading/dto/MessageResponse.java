package com.campustrading.dto;

import com.campustrading.entity.Message;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static MessageResponse fromEntity(Message m) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());
        r.setConversationId(m.getConversationId());
        r.setSenderId(m.getSenderId());
        r.setContent(m.getContent());
        r.setIsRead(m.getIsRead());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
