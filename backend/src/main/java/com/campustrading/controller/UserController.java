package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.UpdateUserRequest;
import com.campustrading.dto.UserInfo;
import com.campustrading.dto.UserProfileResponse;
import com.campustrading.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserInfo> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getUserInfo(userId));
    }

    @GetMapping("/profile/{userId}")
    public Result<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    @PutMapping("/me")
    public Result<UserInfo> updateCurrentUser(Authentication authentication,
                                               @RequestBody UpdateUserRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.updateUserInfo(userId, request));
    }
}
