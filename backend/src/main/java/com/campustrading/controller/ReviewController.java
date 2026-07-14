package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.ReviewRequest;
import com.campustrading.dto.ReviewResponse;
import com.campustrading.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<ReviewResponse> createReview(Authentication authentication,
                                                @Valid @RequestBody ReviewRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(reviewService.createReview(userId, request));
    }

    @GetMapping("/user/{userId}")
    public Result<Page<ReviewResponse>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getUserReviews(userId, page, size));
    }
}
