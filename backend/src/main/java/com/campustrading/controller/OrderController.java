package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.OrderRequest;
import com.campustrading.dto.OrderResponse;
import com.campustrading.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderResponse> createOrder(Authentication authentication,
                                              @Valid @RequestBody OrderRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.createOrder(userId, request));
    }

    @GetMapping
    public Result<Page<OrderResponse>> getMyOrders(Authentication authentication,
                                                    @RequestParam(defaultValue = "all") String role,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.getMyOrders(userId, role, page, size));
    }

    @PutMapping("/{id}/confirm")
    public Result<OrderResponse> confirmOrder(Authentication authentication,
                                               @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.confirmOrder(userId, id));
    }

    @PutMapping("/{id}/complete")
    public Result<OrderResponse> completeOrder(Authentication authentication,
                                                @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.completeOrder(userId, id));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderResponse> cancelOrder(Authentication authentication,
                                              @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.cancelOrder(userId, id));
    }
}
