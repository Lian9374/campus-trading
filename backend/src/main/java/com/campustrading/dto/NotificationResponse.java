package com.campustrading.dto;

import com.campustrading.entity.Notification;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long relatedId;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(notification.getId());
        resp.setType(notification.getType().name());
        resp.setTitle(notification.getTitle());
        resp.setContent(notification.getContent());
        resp.setIsRead(notification.getIsRead());
        resp.setRelatedId(notification.getRelatedId());
        resp.setCreatedAt(notification.getCreatedAt());
        return resp;
    }
}
