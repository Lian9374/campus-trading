package com.campustrading.service;

import com.campustrading.entity.Notification;
import com.campustrading.entity.Order;
import com.campustrading.entity.Product;
import com.campustrading.repository.OrderRepository;
import com.campustrading.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单超时定时任务 — 每 5 分钟执行一次。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    private static final int BATCH_SIZE = 100;

    /**
     * PENDING 超过 24 小时自动取消。
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void autoCancelPendingOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(24);
        int page = 0;
        int cancelled = 0;

        Page<Order> orderPage;
        do {
            orderPage = orderRepository.findByStatusAndCreatedAtBefore(
                    Order.Status.PENDING, deadline, PageRequest.of(page++, BATCH_SIZE));
            for (Order order : orderPage.getContent()) {
                order.setStatus(Order.Status.CANCELLED);
                orderRepository.save(order);

                // 恢复商品
                productRepository.findById(order.getProductId()).ifPresent(product -> {
                    if (product.getStatus() == Product.Status.SOLD) {
                        product.setStatus(Product.Status.ON_SALE);
                        productRepository.save(product);
                    }
                });

                // 通知双方
                String msg = "订单「" + order.getOrderNo() + "」超过 24 小时未确认，已自动取消";
                notificationService.send(order.getBuyerId(), Notification.Type.SYSTEM, "订单自动取消", msg, order.getId());
                notificationService.send(order.getSellerId(), Notification.Type.SYSTEM, "订单自动取消", msg, order.getId());
                cancelled++;
            }
        } while (orderPage.hasNext());

        if (cancelled > 0) {
            log.info("自动取消 {} 个超时 PENDING 订单", cancelled);
        }
    }

    /**
     * CONFIRMED 超过 7 天自动完成。
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void autoCompleteConfirmedOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(7);
        int page = 0;
        int completed = 0;

        Page<Order> orderPage;
        do {
            orderPage = orderRepository.findByStatusAndCreatedAtBefore(
                    Order.Status.CONFIRMED, deadline, PageRequest.of(page++, BATCH_SIZE));
            for (Order order : orderPage.getContent()) {
                order.setStatus(Order.Status.COMPLETED);
                orderRepository.save(order);

                // 通知双方
                String msg = "订单「" + order.getOrderNo() + "」已确认超过 7 天，已自动完成";
                notificationService.send(order.getBuyerId(), Notification.Type.SYSTEM, "订单自动完成", msg, order.getId());
                notificationService.send(order.getSellerId(), Notification.Type.SYSTEM, "订单自动完成", msg + "，快去评价吧", order.getId());
                completed++;
            }
        } while (orderPage.hasNext());

        if (completed > 0) {
            log.info("自动完成 {} 个超时 CONFIRMED 订单", completed);
        }
    }
}
