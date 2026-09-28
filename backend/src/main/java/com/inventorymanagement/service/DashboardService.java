package com.inventorymanagement.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventorymanagement.dto.DashboardSummaryDto;
import com.inventorymanagement.dto.InventoryDocumentResponseDto;
import com.inventorymanagement.dto.PagedResponse;
import com.inventorymanagement.model.DocumentStatus;
import com.inventorymanagement.model.DocumentType;
import com.inventorymanagement.model.InventoryDocument;
import com.inventorymanagement.repository.InventoryDocumentRepository;
import com.inventorymanagement.repository.ProductRepository;
import com.inventorymanagement.repository.StockRepository;

@Service
public class DashboardService {

    private final ProductRepository products;
    private final StockRepository stocks;
    private final InventoryDocumentRepository documents;
    private final InventoryDocumentService documentService;

    public DashboardService(ProductRepository products, StockRepository stocks,
                            InventoryDocumentRepository documents, InventoryDocumentService documentService) {
        this.products = products;
        this.stocks = stocks;
        this.documents = documents;
        this.documentService = documentService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto summary(Pageable pageable) {
        long lowStock = products.findLowStockProducts(null, pageable).getTotalElements();
        return new DashboardSummaryDto(products.countByDeletedAtIsNull(),
                zeroIfNull(stocks.sumAllPhysicalQuantity()), zeroIfNull(stocks.sumInventoryValue()), lowStock,
                documents.countByDocumentTypeAndStatus(DocumentType.RECEIPT, DocumentStatus.DRAFT),
                documents.countByDocumentTypeAndStatus(DocumentType.DELIVERY, DocumentStatus.DRAFT),
                documents.countByDocumentTypeAndStatus(DocumentType.TRANSFER, DocumentStatus.DRAFT),
                documents.countByDocumentTypeAndStatus(DocumentType.ADJUSTMENT, DocumentStatus.DRAFT));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryDocumentResponseDto> documents(DocumentType type, DocumentStatus status, Pageable pageable) {
        Page<InventoryDocument> page;
        if (type != null && status != null) page = documents.findByDocumentTypeAndStatusOrderByCreatedAtDesc(type, status, pageable);
        else if (type != null) page = documents.findByDocumentTypeOrderByCreatedAtDesc(type, pageable);
        else if (status != null) page = documents.findByStatusOrderByCreatedAtDesc(status, pageable);
        else page = documents.findAll(pageable);
        return new PagedResponse<>(page.getContent().stream()
                .map(document -> documentService.get(document.getDocumentType(), document.getId())).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}