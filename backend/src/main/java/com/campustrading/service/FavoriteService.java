package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.ProductResponse;
import com.campustrading.entity.Favorite;
import com.campustrading.entity.Product;
import com.campustrading.entity.ProductImage;
import com.campustrading.repository.FavoriteRepository;
import com.campustrading.repository.ProductImageRepository;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;

    public void addFavorite(Long userId, Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException("商品不存在");
        }

        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException("已收藏该商品");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteRepository.save(favorite);
    }

    public void removeFavorite(Long userId, Long productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new BusinessException("未收藏该商品"));
        favoriteRepository.delete(favorite);
    }

    public Page<ProductResponse> getMyFavorites(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Favorite> favPage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return favPage.map(fav -> {
            Product product = productRepository.findById(fav.getProductId()).orElse(null);
            if (product == null) return null;

            ProductResponse resp = ProductResponse.fromEntity(product);
            userRepository.findById(product.getSellerId())
                    .ifPresent(user -> resp.setSellerName(user.getNickname()));

            List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrder(product.getId());
            resp.setImages(images.stream().map(ProductImage::getUrl).collect(Collectors.toList()));

            return resp;
        });
    }

    public boolean isFavorited(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
