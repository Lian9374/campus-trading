package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.OrderRequest;
import com.campustrading.dto.OrderResponse;
import com.campustrading.entity.Order;
import com.campustrading.entity.Product;
import com.campustrading.entity.User;
import com.campustrading.repository.OrderRepository;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(Long buyerId, OrderRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (product.getStatus() != Product.Status.ON_SALE) {
            throw new BusinessException("商品已售出或已下架");
        }

        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        // 生成订单编号
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setAmount(product.getPrice());
        order.setRemark(request.getRemark());
        order.setStatus(Order.Status.PENDING);
        order = orderRepository.save(order);

        // 商品状态改为已售出
        product.setStatus(Product.Status.SOLD);
        productRepository.save(product);

        return enrichOrderResponse(order);
    }

    public Page<OrderResponse> getMyOrders(Long userId, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;
        if ("buyer".equals(role)) {
            orderPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId, pageable);
        } else if ("seller".equals(role)) {
            orderPage = orderRepository.findBySellerIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            orderPage = orderRepository.findByUserId(userId, pageable);
        }
        return orderPage.map(this::enrichOrderResponse);
    }

    @Transactional
    public OrderResponse confirmOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(403, "只有卖家可以确认订单");
        }

        if (order.getStatus() != Order.Status.PENDING) {
            throw new BusinessException("当前订单状态不允许此操作");
        }

        order.setStatus(Order.Status.CONFIRMED);
        order = orderRepository.save(order);
        return enrichOrderResponse(order);
    }

    @Transactional
    public OrderResponse completeOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(403, "只有买家可以确认收货");
        }

        if (order.getStatus() != Order.Status.CONFIRMED) {
            throw new BusinessException("当前订单状态不允许此操作");
        }

        order.setStatus(Order.Status.COMPLETED);
        order = orderRepository.save(order);
        return enrichOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // PENDING: 买家可取消
        // CONFIRMED: 暂无取消逻辑（MVP阶段不支持卖家取消已确认订单）
        if (order.getStatus() == Order.Status.PENDING) {
            if (!order.getBuyerId().equals(userId)) {
                throw new BusinessException(403, "只有买家可以取消待确认订单");
            }
        } else {
            throw new BusinessException("当前订单状态不允许取消");
        }

        order.setStatus(Order.Status.CANCELLED);
        order = orderRepository.save(order);

        // 恢复商品为在售状态
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product != null && product.getStatus() == Product.Status.SOLD) {
            product.setStatus(Product.Status.ON_SALE);
            productRepository.save(product);
        }

        return enrichOrderResponse(order);
    }

    private OrderResponse enrichOrderResponse(Order order) {
        OrderResponse resp = OrderResponse.fromEntity(order);

        userRepository.findById(order.getBuyerId())
                .ifPresent(user -> resp.setBuyerName(user.getNickname()));
        userRepository.findById(order.getSellerId())
                .ifPresent(user -> resp.setSellerName(user.getNickname()));

        productRepository.findById(order.getProductId()).ifPresent(product -> {
            resp.setProductTitle(product.getTitle());
            resp.setProductCover(product.getCoverImage());
        });

        return resp;
    }
}
