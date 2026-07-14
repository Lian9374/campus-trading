package com.campustrading.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 动作发起者

    @Column(nullable = false, length = 20)
    private String type; // NEW_PRODUCT / SOLD / NEW_REVIEW / PRICE_DROP

    @Column(name = "target_id")
    private Long targetId; // 关联的商品/评价ID

    @Column(length = 500)
    private String summary; // "发布了新商品「xxx」"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
