package com.inventorymanagement.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventorymanagement.dto.ApiResponse;
import com.inventorymanagement.dto.DashboardSummaryDto;
import com.inventorymanagement.dto.InventoryDocumentResponseDto;
import com.inventorymanagement.dto.PagedResponse;
import com.inventorymanagement.model.DocumentStatus;
import com.inventorymanagement.model.DocumentType;
import com.inventorymanagement.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryDto> summary() {
        return ApiResponse.ok(dashboard.summary(PageRequest.of(0, 1)));
    }

    @GetMapping("/documents")
    public ApiResponse<PagedResponse<InventoryDocumentResponseDto>> documents(
            @RequestParam(name = "document_type", required = false) DocumentType documentType,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        var pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(limit, 100)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(dashboard.documents(documentType, status, pageable));
    }
}