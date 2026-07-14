package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.FollowUserResponse;
import com.campustrading.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public Result<Void> follow(Authentication authentication, @PathVariable Long userId) {
        Long followerId = (Long) authentication.getPrincipal();
        followService.follow(followerId, userId);
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(Authentication authentication, @PathVariable Long userId) {
        Long followerId = (Long) authentication.getPrincipal();
        followService.unfollow(followerId, userId);
        return Result.success();
    }

    @GetMapping("/{userId}/followers")
    public Result<Page<FollowUserResponse>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowers(userId, page, size));
    }

    @GetMapping("/{userId}/following")
    public Result<Page<FollowUserResponse>> getFollowings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowings(userId, page, size));
    }

    @GetMapping("/check/{userId}")
    public Result<Map<String, Boolean>> isFollowing(Authentication authentication, @PathVariable Long userId) {
        Long followerId = (Long) authentication.getPrincipal();
        boolean following = followService.isFollowing(followerId, userId);
        return Result.success(Map.of("following", following));
    }

    @GetMapping("/{userId}/stats")
    public Result<Map<String, Long>> getStats(@PathVariable Long userId) {
        long followers = followService.getFollowerCount(userId);
        long following = followService.getFollowingCount(userId);
        return Result.success(Map.of("followers", followers, "following", following));
    }
}
