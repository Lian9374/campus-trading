package com.campustrading.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String phone;
    private String campus;
    private String avatar;
}
