package com.campustrading.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private Long productId;      // 举报商品时传此字段

    private Long targetUserId;   // 举报用户时传此字段

    private String reason;       // 举报原因

    private String detail;       // 详细说明
}
