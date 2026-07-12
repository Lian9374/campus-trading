package com.campustrading.dto;

import com.campustrading.entity.Product;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private String status;
    private String coverImage;
    private List<String> images;
    private Integer viewCount;
    private LocalDateTime createdAt;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse resp = new ProductResponse();
        resp.setId(product.getId());
        resp.setTitle(product.getTitle());
        resp.setDescription(product.getDescription());
        resp.setPrice(product.getPrice());
        resp.setOriginalPrice(product.getOriginalPrice());
        resp.setCategoryId(product.getCategoryId());
        resp.setSellerId(product.getSellerId());
        resp.setStatus(product.getStatus().name());
        resp.setCoverImage(product.getCoverImage());
        resp.setViewCount(product.getViewCount());
        resp.setCreatedAt(product.getCreatedAt());
        return resp;
    }
}
