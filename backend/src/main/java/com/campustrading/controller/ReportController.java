package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.ReportRequest;
import com.campustrading.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reports")
    public Result<Void> createReport(Authentication authentication,
                                      @Valid @RequestBody ReportRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        reportService.createReport(userId, request);
        return Result.success();
    }
}
