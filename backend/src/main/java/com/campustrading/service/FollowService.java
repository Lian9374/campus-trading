package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.FollowUserResponse;
import com.campustrading.entity.Follow;
import com.campustrading.entity.Product;
import com.campustrading.entity.User;
import com.campustrading.repository.FollowRepository;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }

        User target = userRepository.findById(followingId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException("已关注该用户");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        followRepository.save(follow);

        // 通知被关注者
        User follower = userRepository.findById(followerId).orElse(null);
        String followerName = follower != null ? follower.getNickname() : "用户";
        notificationService.send(followingId,
                com.campustrading.entity.Notification.Type.SYSTEM,
                "新粉丝",
                followerName + " 关注了您",
                null);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new BusinessException("未关注该用户"));
        followRepository.delete(follow);
    }

    public Page<FollowUserResponse> getFollowings(Long userId, int page, int size) {
        page = Math.max(0, page);
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followPage = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);
        return mapToFollowUserResponse(followPage, true);
    }

    public Page<FollowUserResponse> getFollowers(Long userId, int page, int size) {
        page = Math.max(0, page);
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followPage = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);
        return mapToFollowUserResponse(followPage, false);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public long getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    private Page<FollowUserResponse> mapToFollowUserResponse(Page<Follow> followPage, boolean isFollowing) {
        List<Follow> follows = followPage.getContent();
        if (follows.isEmpty()) {
            return new PageImpl<>(List.of(), followPage.getPageable(), 0);
        }

        // 批量加载用户信息（关注的人 或 粉丝）
        List<Long> userIds = follows.stream()
                .map(f -> isFollowing ? f.getFollowingId() : f.getFollowerId())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量统计在售商品数
        Map<Long, Long> productCountMap = userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> productRepository.countBySellerIdAndStatus(id, Product.Status.ON_SALE)
                ));

        List<FollowUserResponse> list = follows.stream().map(f -> {
            FollowUserResponse r = new FollowUserResponse();
            r.setFollowedAt(f.getCreatedAt());

            Long uid = isFollowing ? f.getFollowingId() : f.getFollowerId();
            r.setUserId(uid);

            User user = userMap.get(uid);
            if (user != null) {
                r.setNickname(user.getNickname());
                r.setAvatar(user.getAvatar());
                r.setCampus(user.getCampus());
            }
            r.setProductCount(productCountMap.getOrDefault(uid, 0L).intValue());
            return r;
        }).collect(Collectors.toList());

        return new PageImpl<>(list, followPage.getPageable(), followPage.getTotalElements());
    }
}
