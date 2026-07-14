package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.CommentRequest;
import com.campustrading.dto.CommentResponse;
import com.campustrading.service.ProductCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class ProductCommentController {

    private final ProductCommentService commentService;

    @PostMapping
    public Result<CommentResponse> addComment(Authentication authentication,
                                               @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(commentService.addComment(userId, request));
    }

    @GetMapping("/product/{productId}")
    public Result<Page<CommentResponse>> getComments(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.getComments(productId, page, size));
    }
}
