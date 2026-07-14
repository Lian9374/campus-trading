package com.campustrading.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "product_id")
    private Long productId; // nullable: 举报用户时为 null

    @Column(name = "target_user_id")
    private Long targetUserId; // 被举报的用户ID

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType = "PRODUCT"; // PRODUCT / USER

    @Column(nullable = false, length = 50)
    private String reason; // 虚假信息 / 违禁品 / 侵权 / 其他

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "handler_note", length = 500)
    private String handlerNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, RESOLVED, DISMISSED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
