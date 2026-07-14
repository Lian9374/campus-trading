package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.entity.Activity;
import com.campustrading.repository.ActivityRepository;
import com.campustrading.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityRepository activityRepository;
    private final FollowRepository followRepository;

    @GetMapping("/feed")
    public Result<Page<Activity>> getFeed(Authentication authentication,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        int p = Math.max(0, page);
        int s = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(p, s);

        // 获取关注的用户ID列表
        List<Long> followingIds = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId,
                PageRequest.of(0, 500)).getContent().stream()
                .map(f -> f.getFollowingId())
                .collect(Collectors.toList());

        if (followingIds.isEmpty()) {
            return Result.success(Page.empty(pageable));
        }

        return Result.success(activityRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable));
    }

    @GetMapping("/user/{userId}")
    public Result<Page<Activity>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int p = Math.max(0, page);
        int s = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(p, s);
        return Result.success(activityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable));
    }
}
