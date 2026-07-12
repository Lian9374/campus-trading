package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.ProductRequest;
import com.campustrading.dto.ProductResponse;
import com.campustrading.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<Page<ProductResponse>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.success(productService.listProducts(keyword, categoryId, minPrice, maxPrice, page, size));
    }

    @GetMapping("/{id}")
    public Result<ProductResponse> getProduct(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @PostMapping
    public Result<ProductResponse> createProduct(Authentication authentication,
                                                  @Valid @RequestBody ProductRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(productService.createProduct(userId, request));
    }

    @PutMapping("/{id}")
    public Result<ProductResponse> updateProduct(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(productService.updateProduct(userId, id, request));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(Authentication authentication,
                                      @PathVariable Long id,
                                      @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        productService.updateProductStatus(userId, id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<ProductResponse>> getMyProducts(Authentication authentication,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "12") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(productService.getMyProducts(userId, page, size));
    }
}
