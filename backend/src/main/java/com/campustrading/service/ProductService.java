package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.ProductRequest;
import com.campustrading.dto.ProductResponse;
import com.campustrading.entity.Category;
import com.campustrading.entity.Product;
import com.campustrading.entity.ProductImage;
import com.campustrading.entity.User;
import com.campustrading.repository.CategoryRepository;
import com.campustrading.repository.ProductImageRepository;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public Page<ProductResponse> listProducts(String keyword, Integer categoryId,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 只查询在售商品
            predicates.add(cb.equal(root.get("status"), Product.Status.ON_SALE));

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("title"), "%" + keyword + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(this::enrichProductResponse);
    }

    public ProductResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 增加浏览量
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        return enrichProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(Long sellerId, ProductRequest request) {
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategoryId(request.getCategoryId());
        product.setSellerId(sellerId);
        product.setCoverImage(request.getCoverImage());
        product = productRepository.save(product);

        // 保存图片列表
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            saveProductImages(product.getId(), request.getImages());
        }

        return enrichProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long userId, Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权修改此商品");
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategoryId(request.getCategoryId());
        product.setCoverImage(request.getCoverImage());
        product = productRepository.save(product);

        // 更新图片列表
        if (request.getImages() != null) {
            productImageRepository.deleteByProductId(productId);
            saveProductImages(productId, request.getImages());
        }

        return enrichProductResponse(product);
    }

    public void updateProductStatus(Long userId, Long productId, String status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权操作此商品");
        }

        try {
            product.setStatus(Product.Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的商品状态");
        }
        productRepository.save(product);
    }

    public Page<ProductResponse> getMyProducts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findBySellerId(userId, pageable).map(this::enrichProductResponse);
    }

    private void saveProductImages(Long productId, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(imageUrls.get(i));
            image.setSortOrder(i);
            productImageRepository.save(image);
        }
    }

    private ProductResponse enrichProductResponse(Product product) {
        ProductResponse resp = ProductResponse.fromEntity(product);

        // 分类名称
        if (product.getCategoryId() != null) {
            categoryRepository.findById(product.getCategoryId())
                    .ifPresent(cat -> resp.setCategoryName(cat.getName()));
        }

        // 卖家昵称
        userRepository.findById(product.getSellerId())
                .ifPresent(user -> resp.setSellerName(user.getNickname()));

        // 图片列表
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrder(product.getId());
        resp.setImages(images.stream().map(ProductImage::getUrl).collect(Collectors.toList()));

        return resp;
    }
}
