package com.stockroom.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockroom.dto.ApiResponse;
import com.stockroom.dto.InventoryDocumentRequestDto;
import com.stockroom.dto.InventoryDocumentResponseDto;
import com.stockroom.dto.PagedResponse;
import com.stockroom.exception.ResourceNotFoundException;
import com.stockroom.model.DocumentType;
import com.stockroom.service.InventoryDocumentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/{collection:receipts|deliveries|transfers|adjustments}")
public class InventoryDocumentController {

    private final InventoryDocumentService documents;

    public InventoryDocumentController(InventoryDocumentService documents) {
        this.documents = documents;
    }

    @GetMapping
    public ApiResponse<PagedResponse<InventoryDocumentResponseDto>> list(@PathVariable String collection,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit) {
        Pageable pageable = pageable(page, limit);
        return ApiResponse.ok(documents.list(type(collection), pageable));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryDocumentResponseDto>> create(@PathVariable String collection,
            @Valid @RequestBody InventoryDocumentRequestDto request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(documents.create(type(collection), request,
                authentication == null ? "system" : authentication.getName())));
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryDocumentResponseDto> get(@PathVariable String collection, @PathVariable Long id) {
        return ApiResponse.ok(documents.get(type(collection), id));
    }

    @RequestMapping(path = "/{id}/validate", method = {RequestMethod.PUT, RequestMethod.POST})
    public ApiResponse<InventoryDocumentResponseDto> validate(@PathVariable String collection, @PathVariable Long id,
                                                               Authentication authentication) {
        return ApiResponse.ok(documents.validate(type(collection), id,
                authentication == null ? "system" : authentication.getName()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<InventoryDocumentResponseDto> cancel(@PathVariable String collection, @PathVariable Long id) {
        return ApiResponse.ok(documents.cancel(type(collection), id));
    }

    private DocumentType type(String collection) {
        return switch (collection) {
            case "receipts" -> DocumentType.RECEIPT;
            case "deliveries" -> DocumentType.DELIVERY;
            case "transfers" -> DocumentType.TRANSFER;
            case "adjustments" -> DocumentType.ADJUSTMENT;
            default -> throw new ResourceNotFoundException("Unknown inventory document collection");
        };
    }

    private Pageable pageable(int page, int limit) {
        return PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(limit, 100)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}