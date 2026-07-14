package com.campustrading.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowUserResponse {

    private Long userId;
    private String nickname;
    private String avatar;
    private String campus;
    private int productCount;
    private LocalDateTime followedAt;
}
