package com.inventorymanagement.controller;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventorymanagement.dto.ApiResponse;
import com.inventorymanagement.dto.LedgerEntryDto;
import com.inventorymanagement.dto.PagedResponse;
import com.inventorymanagement.dto.ProductRequestDto;
import com.inventorymanagement.dto.ProductResponseDto;
import com.inventorymanagement.service.ProductService;
import com.inventorymanagement.service.StockLedgerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService products;
    private final StockLedgerService ledger;

    public ProductController(ProductService products, StockLedgerService ledger) {
        this.products = products;
        this.ledger = ledger;
    }

    @GetMapping
    public ApiResponse<PagedResponse<ProductResponseDto>> list(
            @RequestParam(required = false) String search,
            @RequestParam(name = "category_id", required = false) Long categoryId,
            @RequestParam(name = "low_stock", required = false) Boolean lowStock,
            @RequestParam(name = "warehouse_id", required = false) Long warehouseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(name = "sort_by", defaultValue = "created_at") String sortBy,
            @RequestParam(name = "sort_order", defaultValue = "desc") String sortOrder) {
        String property = switch (sortBy) {
            case "name" -> "name";
            case "sku" -> "sku";
            case "per_unit_cost" -> "perUnitCost";
            case "reorder_point" -> "reorderPoint";
            case "created_at" -> "createdAt";
            default -> throw new IllegalArgumentException("Unsupported sort_by value");
        };
        Sort sort = "asc".equalsIgnoreCase(sortOrder) ? Sort.by(property).ascending() : Sort.by(property).descending();
        return ApiResponse.ok(products.list(search, categoryId, lowStock, warehouseId, pageable(page, limit, sort)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> create(@Valid @RequestBody ProductRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(products.create(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDto> get(@PathVariable Long id) {
        return ApiResponse.ok(products.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponseDto> update(@PathVariable Long id, @Valid @RequestBody ProductRequestDto request) {
        return ApiResponse.ok(products.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, String>> delete(@PathVariable Long id) {
        products.delete(id);
        return ApiResponse.ok(Map.of("message", "Product deleted successfully"));
    }

    @GetMapping("/{id}/ledger")
    public ApiResponse<PagedResponse<LedgerEntryDto>> productLedger(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(ledger.listForProduct(id, pageable(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    private Pageable pageable(int page, int limit, Sort sort) {
        return PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(limit, 100)), sort);
    }
}