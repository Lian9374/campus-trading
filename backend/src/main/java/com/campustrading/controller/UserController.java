package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.UpdateUserRequest;
import com.campustrading.dto.UserInfo;
import com.campustrading.dto.UserProfileResponse;
import com.campustrading.entity.User;
import com.campustrading.repository.UserRepository;
import com.campustrading.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public Result<UserInfo> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getUserInfo(userId));
    }

    @GetMapping("/profile/{userId}")
    public Result<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    @GetMapping("/search")
    public Result<Page<UserInfo>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int p = Math.max(0, page);
        int s = Math.min(50, Math.max(1, size));
        Page<User> userPage = userRepository.searchByKeyword(keyword, PageRequest.of(p, s));
        return Result.success(userPage.map(UserInfo::fromEntity));
    }

    @PutMapping("/me")
    public Result<UserInfo> updateCurrentUser(Authentication authentication,
                                               @RequestBody UpdateUserRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.updateUserInfo(userId, request));
    }
}
