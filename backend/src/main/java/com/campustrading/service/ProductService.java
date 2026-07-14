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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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
                                               String campus, String sortBy,
                                               int page, int size) {
        // 分页参数防护
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));

        // 动态排序
        Sort sort;
        switch (sortBy != null ? sortBy : "newest") {
            case "price_asc":  sort = Sort.by(Sort.Direction.ASC, "price"); break;
            case "price_desc": sort = Sort.by(Sort.Direction.DESC, "price"); break;
            default:           sort = Sort.by(Sort.Direction.DESC, "createdAt"); break;
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        // 校区筛选：先查同校区用户
        Set<Long> campusSellerIds = null;
        if (campus != null && !campus.isBlank()) {
            campusSellerIds = userRepository.findAll().stream()
                    .filter(u -> campus.equals(u.getCampus()))
                    .map(User::getId)
                    .collect(Collectors.toSet());
            if (campusSellerIds.isEmpty()) {
                // 没有该校区用户 → 返回空
                return new PageImpl<>(List.of(), pageable, 0);
            }
        }

        final Set<Long> finalCampusSellerIds = campusSellerIds;
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
            if (finalCampusSellerIds != null) {
                predicates.add(root.get("sellerId").in(finalCampusSellerIds));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductResponse> enriched = batchEnrichProductResponses(productPage.getContent());
        return new PageImpl<>(enriched, pageable, productPage.getTotalElements());
    }

    @Transactional
    public ProductResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 原子更新浏览量，避免竞态条件
        productRepository.incrementViewCount(id);

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
        // 分页参数防护
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findBySellerId(userId, pageable);
        List<ProductResponse> enriched = batchEnrichProductResponses(productPage.getContent());
        return new PageImpl<>(enriched, pageable, productPage.getTotalElements());
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

    /**
     * 批量加载关联数据（分类名、卖家昵称、图片列表），避免 N+1 查询。
     * 面向所有 Product 列表场景（首页、我的发布、收藏等）。
     */
    public List<ProductResponse> batchEnrichProductResponses(List<Product> products) {
        if (products.isEmpty()) return List.of();

        // 收集所有需要查询的 ID
        Set<Long> sellerIds = products.stream()
                .map(Product::getSellerId).collect(Collectors.toSet());
        Set<Integer> categoryIds = products.stream()
                .map(Product::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Long> productIds = products.stream()
                .map(Product::getId).collect(Collectors.toList());

        // 批量查询
        Map<Long, User> userMap = userRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, Category> catMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
        Map<Long, List<String>> imageMap = productImageRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.groupingBy(
                        ProductImage::getProductId,
                        LinkedHashMap::new,
                        Collectors.mapping(ProductImage::getUrl, Collectors.toList())));

        // 组装
        return products.stream().map(p -> {
            ProductResponse resp = ProductResponse.fromEntity(p);
            if (p.getCategoryId() != null) {
                Category cat = catMap.get(p.getCategoryId());
                if (cat != null) resp.setCategoryName(cat.getName());
            }
            User seller = userMap.get(p.getSellerId());
            if (seller != null) resp.setSellerName(seller.getNickname());
            resp.setImages(imageMap.getOrDefault(p.getId(), List.of()));
            return resp;
        }).collect(Collectors.toList());
    }

    /**
     * 单品 enrichment（详情页等场景），内部复用批量方法。
     */
    public ProductResponse enrichProductResponse(Product product) {
        return batchEnrichProductResponses(List.of(product)).get(0);
    }
}
