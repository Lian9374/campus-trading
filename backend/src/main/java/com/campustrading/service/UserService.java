package com.campustrading.service;

import com.campustrading.dto.UpdateUserRequest;
import com.campustrading.dto.UserInfo;
import com.campustrading.dto.UserProfileResponse;
import com.campustrading.entity.Product;
import com.campustrading.entity.User;
import com.campustrading.common.BusinessException;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public UserInfo getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return UserInfo.fromEntity(user);
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        UserProfileResponse profile = UserProfileResponse.fromEntity(user);

        // 统计在售和已售商品数量
        long onSaleCount = productRepository.countBySellerIdAndStatus(userId, Product.Status.ON_SALE);
        long soldCount = productRepository.countBySellerIdAndStatus(userId, Product.Status.SOLD);
        profile.setProductCount((int) onSaleCount);
        profile.setSoldCount((int) soldCount);

        return profile;
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
