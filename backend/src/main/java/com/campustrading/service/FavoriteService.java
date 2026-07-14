package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.ProductResponse;
import com.campustrading.entity.Favorite;
import com.campustrading.entity.Product;
import com.campustrading.repository.FavoriteRepository;
import com.campustrading.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

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
        // 分页参数防护
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));

        Pageable pageable = PageRequest.of(page, size);
        Page<Favorite> favPage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // 收集所有收藏的商品 ID，批量加载
        List<Long> productIds = favPage.getContent().stream()
                .map(Favorite::getProductId).collect(Collectors.toList());

        // 批量查询 + 构建 ID→Product 映射（处理已删除商品）
        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 按收藏顺序组装，跳过已删除的商品
        List<Product> orderedProducts = favPage.getContent().stream()
                .map(fav -> productMap.get(fav.getProductId()))
                .filter(p -> p != null)
                .collect(Collectors.toList());

        // 批量 enrichment（消除 N+1 查询）
        List<ProductResponse> enriched = productService.batchEnrichProductResponses(orderedProducts);

        return new PageImpl<>(enriched, pageable, favPage.getTotalElements());
    }

    public boolean isFavorited(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
