package com.campustrading.controller;

import com.campustrading.common.BusinessException;
import com.campustrading.common.Result;
import com.campustrading.dto.ProductResponse;
import com.campustrading.dto.ReportResponse;
import com.campustrading.entity.Order;
import com.campustrading.entity.Product;
import com.campustrading.entity.Report;
import com.campustrading.entity.User;
import com.campustrading.repository.OrderRepository;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.ReportRepository;
import com.campustrading.repository.UserRepository;
import com.campustrading.service.ProductService;
import com.campustrading.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ReportService reportService;
    private final ReportRepository reportRepository;

    // ==================== 统计 ====================

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        long userCount = userRepository.count();
        long productCount = productRepository.count();
        long onSaleCount = productRepository.countByStatus(Product.Status.ON_SALE);
        long orderCount = orderRepository.count();
        long completedOrderCount = orderRepository.countByStatus(Order.Status.COMPLETED);
        BigDecimal totalRevenue = orderRepository.sumAmountByStatus(Order.Status.COMPLETED);
        long pendingReportCount = reportRepository.countByStatus(Report.Status.PENDING);

        return Result.success(Map.of(
                "userCount", userCount,
                "productCount", productCount,
                "onSaleCount", onSaleCount,
                "orderCount", orderCount,
                "completedOrderCount", completedOrderCount,
                "totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                "pendingReportCount", pendingReportCount
        ));
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<Page<User>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage;
        if (keyword != null && !keyword.isBlank()) {
            userPage = userRepository.findAll(pageable); // simplified: find all, frontend filters
        } else {
            userPage = userRepository.findAll(pageable);
        }

        // 脱敏：清除密码
        userPage.forEach(u -> u.setPassword(null));
        return Result.success(userPage);
    }

    @PutMapping("/users/{id}/ban")
    public Result<Void> banUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getRole() == User.Role.ADMIN) {
            throw new BusinessException("不能封禁管理员");
        }

        user.setStatus(user.getStatus() == User.Status.BANNED
                ? User.Status.ACTIVE : User.Status.BANNED);
        userRepository.save(user);
        return Result.success();
    }

    // ==================== 商品管理 ====================

    @GetMapping("/products")
    public Result<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        page = Math.max(0, page);
        size = Math.min(100, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> productPage;
        if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.findByTitleContaining(keyword, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return Result.success(productPage.map(productService::enrichProductResponse));
    }

    @PutMapping("/products/{id}/force-remove")
    public Result<Void> forceRemoveProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        product.setStatus(Product.Status.REMOVED);
        productRepository.save(product);
        return Result.success();
    }

    // ==================== 举报管理 ====================

    @GetMapping("/reports")
    public Result<Page<ReportResponse>> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(reportService.getReports(status, page, size));
    }

    @PutMapping("/reports/{id}/handle")
    public Result<ReportResponse> handleReport(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String action = body.get("action"); // resolve / dismiss
        String handlerNote = body.get("handlerNote");
        return Result.success(reportService.handleReport(id, action, handlerNote));
    }
}
