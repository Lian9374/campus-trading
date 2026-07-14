package com.campustrading.dto;

import com.campustrading.entity.Report;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long productId;
    private String productTitle;
    private String reason;
    private String detail;
    private String status;
    private String handlerNote;
    private LocalDateTime createdAt;

    public static ReportResponse fromEntity(Report report) {
        ReportResponse resp = new ReportResponse();
        resp.setId(report.getId());
        resp.setReporterId(report.getReporterId());
        resp.setProductId(report.getProductId());
        resp.setReason(report.getReason());
        resp.setDetail(report.getDetail());
        resp.setStatus(report.getStatus().name());
        resp.setHandlerNote(report.getHandlerNote());
        resp.setCreatedAt(report.getCreatedAt());
        return resp;
    }
}
