package com.campustrading.service;

import com.campustrading.common.BusinessException;
import com.campustrading.dto.ReportRequest;
import com.campustrading.dto.ReportResponse;
import com.campustrading.entity.Product;
import com.campustrading.entity.Report;
import com.campustrading.repository.ProductRepository;
import com.campustrading.repository.ReportRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void createReport(Long reporterId, ReportRequest request) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReason(request.getReason());
        report.setDetail(request.getDetail());

        if (request.getProductId() != null) {
            // 举报商品
            if (!productRepository.existsById(request.getProductId())) {
                throw new BusinessException("商品不存在");
            }
            if (reportRepository.existsByReporterIdAndProductId(reporterId, request.getProductId())) {
                throw new BusinessException("您已举报过该商品");
            }
            report.setProductId(request.getProductId());
            report.setTargetType("PRODUCT");
        } else if (request.getTargetUserId() != null) {
            // 举报用户
            if (reporterId.equals(request.getTargetUserId())) {
                throw new BusinessException("不能举报自己");
            }
            if (!userRepository.existsById(request.getTargetUserId())) {
                throw new BusinessException("用户不存在");
            }
            report.setTargetUserId(request.getTargetUserId());
            report.setTargetType("USER");
        } else {
            throw new BusinessException("请指定举报对象");
        }

        reportRepository.save(report);
    }

    public Page<ReportResponse> getReports(String statusFilter, int page, int size) {
        page = Math.max(0, page);
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size);

        Page<Report> reportPage;
        if (statusFilter != null && !statusFilter.isBlank()) {
            reportPage = reportRepository.findByStatusOrderByCreatedAtDesc(
                    Report.Status.valueOf(statusFilter.toUpperCase()), pageable);
        } else {
            reportPage = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<ReportResponse> enriched = reportPage.getContent().stream()
                .map(this::enrich).collect(Collectors.toList());
        return new PageImpl<>(enriched, pageable, reportPage.getTotalElements());
    }

    @Transactional
    public ReportResponse handleReport(Long reportId, String action, String handlerNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("举报不存在"));

        if (report.getStatus() != Report.Status.PENDING) {
            throw new BusinessException("该举报已处理");
        }

        Report.Status targetStatus;
        if ("resolve".equals(action)) {
            targetStatus = Report.Status.RESOLVED;
        } else if ("dismiss".equals(action)) {
            targetStatus = Report.Status.DISMISSED;
        } else {
            throw new BusinessException("无效的处理动作，仅支持 resolve / dismiss");
        }

        report.setStatus(targetStatus);
        report.setHandlerNote(handlerNote);
        report = reportRepository.save(report);

        // RESOLVED → 自动下架商品
        if (targetStatus == Report.Status.RESOLVED) {
            productRepository.findById(report.getProductId()).ifPresent(product -> {
                product.setStatus(Product.Status.REMOVED);
                productRepository.save(product);
            });
        }

        return enrich(report);
    }

    private ReportResponse enrich(Report report) {
        ReportResponse resp = ReportResponse.fromEntity(report);
        userRepository.findById(report.getReporterId())
                .ifPresent(u -> resp.setReporterName(u.getNickname()));
        if (report.getProductId() != null) {
            productRepository.findById(report.getProductId())
                    .ifPresent(p -> resp.setProductTitle(p.getTitle()));
        }
        return resp;
    }
}
