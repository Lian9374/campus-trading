package com.campustrading.dto;

import com.campustrading.entity.User;
import lombok.Data;

@Data
public class UserInfo {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private String campus;

    public static UserInfo fromEntity(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setNickname(user.getNickname());
        info.setPhone(user.getPhone());
        info.setAvatar(user.getAvatar());
        info.setCampus(user.getCampus());
        return info;
    }
}
