package com.campustrading.dto;

import com.campustrading.entity.Order;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private String orderNo;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private Long productId;
    private String productTitle;
    private String productCover;
    private BigDecimal amount;
    private String status;
    private String remark;
    private LocalDateTime createdAt;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        resp.setBuyerId(order.getBuyerId());
        resp.setSellerId(order.getSellerId());
        resp.setProductId(order.getProductId());
        resp.setAmount(order.getAmount());
        resp.setStatus(order.getStatus().name());
        resp.setRemark(order.getRemark());
        resp.setCreatedAt(order.getCreatedAt());
        return resp;
    }
}
