package com.campustrading.service;

import com.campustrading.dto.UpdateUserRequest;
import com.campustrading.dto.UserInfo;
import com.campustrading.entity.User;
import com.campustrading.common.BusinessException;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserInfo getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return UserInfo.fromEntity(user);
    }

    public UserInfo updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getCampus() != null) {
            user.setCampus(request.getCampus());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userRepository.save(user);
        return UserInfo.fromEntity(user);
    }
}
