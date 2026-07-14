package com.campustrading.repository;

import com.campustrading.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Report> findByStatusOrderByCreatedAtDesc(Report.Status status, Pageable pageable);
    boolean existsByReporterIdAndProductId(Long reporterId, Long productId);
    long countByStatus(Report.Status status);
}
