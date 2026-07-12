package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.ProductResponse;
import com.campustrading.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public Result<Void> addFavorite(Authentication authentication,
                                     @RequestBody Map<String, Long> body) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.addFavorite(userId, body.get("productId"));
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> removeFavorite(Authentication authentication,
                                        @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.removeFavorite(userId, productId);
        return Result.success();
    }

    @GetMapping
    public Result<Page<ProductResponse>> getMyFavorites(Authentication authentication,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "12") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(favoriteService.getMyFavorites(userId, page, size));
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Boolean>> checkFavorite(Authentication authentication,
                                                       @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(Map.of("favorited", favoriteService.isFavorited(userId, productId)));
    }
}
