package com.campustrading.dto;

import com.campustrading.entity.User;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {

    private Long id;
    private String nickname;
    private String avatar;
    private String campus;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private int productCount;   // 在售商品数
    private int soldCount;      // 已售商品数
    private LocalDateTime joinedAt;

    public static UserProfileResponse fromEntity(User user) {
        UserProfileResponse r = new UserProfileResponse();
        r.setId(user.getId());
        r.setNickname(user.getNickname());
        r.setAvatar(user.getAvatar());
        r.setCampus(user.getCampus());
        r.setRatingAvg(user.getRatingAvg());
        r.setRatingCount(user.getRatingCount());
        r.setJoinedAt(user.getCreatedAt());
        return r;
    }
}
