package com.campustrading.repository;

import com.campustrading.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderBySortOrder(Long productId);
    List<ProductImage> findByProductIdIn(List<Long> productIds);
    void deleteByProductId(Long productId);
}
