package com.inventorymanagement.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventorymanagement.dto.ApiResponse;
import com.inventorymanagement.dto.ApplyMovementRequest;
import com.inventorymanagement.dto.LedgerEntryDto;
import com.inventorymanagement.dto.PagedResponse;
import com.inventorymanagement.model.MovementType;
import com.inventorymanagement.service.StockLedgerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ledger")
public class StockLedgerController {

    private final StockLedgerService ledger;

    public StockLedgerController(StockLedgerService ledger) {
        this.ledger = ledger;
    }

    @PostMapping("/movement")
    public ResponseEntity<ApiResponse<LedgerEntryDto>> apply(@Valid @RequestBody ApplyMovementRequest request,
                                                              Authentication authentication) {
        String actor = authentication == null ? "system" : authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ledger.applyMovement(request, actor)));
    }

    @GetMapping
    public ApiResponse<PagedResponse<LedgerEntryDto>> list(
            @RequestParam(name = "product_id", required = false) Long productId,
            @RequestParam(name = "location_id", required = false) Long locationId,
            @RequestParam(name = "movement_type", required = false) MovementType movementType,
            @RequestParam(name = "from_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(name = "to_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(limit, 100)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(ledger.list(productId, locationId, movementType, fromDate, toDate, search, pageable));
    }
}