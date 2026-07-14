package com.campustrading.repository;

import com.campustrading.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    List<Product> findBySellerId(Long sellerId);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    List<Product> findBySellerIdIn(List<Long> sellerIds);

    Page<Product> findByTitleContaining(String title, Pageable pageable);

    long countByStatus(Product.Status status);
}
