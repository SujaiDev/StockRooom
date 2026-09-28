package com.inventorymanagement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventorymanagement.dto.ApplyMovementRequest;
import com.inventorymanagement.dto.LedgerEntryDto;
import com.inventorymanagement.dto.PagedResponse;
import com.inventorymanagement.exception.InsufficientStockException;
import com.inventorymanagement.exception.ResourceNotFoundException;
import com.inventorymanagement.model.Location;
import com.inventorymanagement.model.MovementType;
import com.inventorymanagement.model.Product;
import com.inventorymanagement.model.Stock;
import com.inventorymanagement.model.StockLedger;
import com.inventorymanagement.repository.LocationRepository;
import com.inventorymanagement.repository.ProductRepository;
import com.inventorymanagement.repository.StockLedgerRepository;
import com.inventorymanagement.repository.StockRepository;

@Service
public class StockLedgerService {

    private static final long VENDOR_LOCATION_ID = 1L;
    private static final long CUSTOMER_LOCATION_ID = 2L;
    private static final long ADJUSTMENT_LOCATION_ID = 3L;

    private final ProductRepository products;
    private final LocationRepository locations;
    private final StockRepository stocks;
    private final StockLedgerRepository ledger;

    public StockLedgerService(ProductRepository products, LocationRepository locations,
                              StockRepository stocks, StockLedgerRepository ledger) {
        this.products = products;
        this.locations = locations;
        this.stocks = stocks;
        this.ledger = ledger;
    }

    @Transactional
    public LedgerEntryDto applyMovement(ApplyMovementRequest request, String actor) {
        if (request.quantity() == null || request.quantity().signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        Product product = products.findActiveByIdForUpdate(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        Long fromId = request.locationFromId();
        Long toId = request.locationToId();
        if (request.movementType() == MovementType.RECEIPT && fromId == null) fromId = VENDOR_LOCATION_ID;
        if (request.movementType() == MovementType.DELIVERY && toId == null) toId = CUSTOMER_LOCATION_ID;
        if (request.movementType() == MovementType.ADJUSTMENT) {
            if (fromId == null && toId != null) fromId = ADJUSTMENT_LOCATION_ID;
            else if (toId == null && fromId != null) toId = ADJUSTMENT_LOCATION_ID;
        }
        if (fromId == null || toId == null) {
            throw new IllegalArgumentException("Both source and destination locations are required for this movement");
        }
        if (fromId.equals(toId)) throw new IllegalArgumentException("Source and destination locations must differ");

        Long sourceLocationId = fromId;
        Long destinationLocationId = toId;
        Location from = locations.findById(sourceLocationId)
            .orElseThrow(() -> new ResourceNotFoundException("Source location not found: " + sourceLocationId));
        Location to = locations.findById(destinationLocationId)
            .orElseThrow(() -> new ResourceNotFoundException("Destination location not found: " + destinationLocationId));
        validateMovementLocations(request.movementType(), from, to);

        Stock sourceStock = null;
        if (!from.isVirtual()) {
            sourceStock = stocks.findByProductIdAndLocationIdForUpdate(product.getId(), from.getId()).orElse(null);
            BigDecimal available = sourceStock == null ? BigDecimal.ZERO : sourceStock.getQuantity();
            if (available.compareTo(request.quantity()) < 0) {
                throw new InsufficientStockException(product.getId(), from.getId(), available, request.quantity());
            }
        }

        StockLedger entry = new StockLedger();
        entry.setProduct(product);
        entry.setLocationFrom(from);
        entry.setLocationTo(to);
        entry.setMovementType(request.movementType());
        entry.setReferenceDocType(request.referenceDocType());
        entry.setReferenceDocId(request.referenceDocId());
        entry.setQuantity(request.quantity());
        entry.setStatus("DONE");
        entry.setCreatedBy(actor == null || actor.isBlank() ? "system" : actor);
        entry.setCreatedAt(LocalDateTime.now());
        entry = ledger.saveAndFlush(entry);

        if (sourceStock != null) {
            sourceStock.setQuantity(sourceStock.getQuantity().subtract(request.quantity()));
            sourceStock.setUpdatedAt(LocalDateTime.now());
            stocks.save(sourceStock);
        }
        if (!to.isVirtual()) {
            Stock destinationStock = stocks.findByProductIdAndLocationIdForUpdate(product.getId(), to.getId())
                    .orElseGet(() -> newStock(product, to));
            destinationStock.setQuantity(destinationStock.getQuantity().add(request.quantity()));
            destinationStock.setUpdatedAt(LocalDateTime.now());
            stocks.save(destinationStock);
        }

        BigDecimal runningBalance = ledger.computeRunningBalance(product.getId(), entry.getId());
        return toResponse(entry, runningBalance);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryDto> list(Long productId, Long locationId, MovementType movementType,
                                              LocalDateTime fromDate, LocalDateTime toDate,
                                              String search, Pageable pageable) {
        Page<StockLedger> page = ledger.findWithFilters(productId, locationId, movementType,
                fromDate, toDate, search == null || search.isBlank() ? null : search.trim(), pageable);
        return new PagedResponse<>(page.getContent().stream()
                .map(entry -> toResponse(entry, ledger.computeRunningBalance(entry.getProduct().getId(), entry.getId())))
                .toList(), page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryDto> listForProduct(Long productId, Pageable pageable) {
        products.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Page<StockLedger> page = ledger.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        List<LedgerEntryDto> entries = page.getContent().stream()
                .map(entry -> toResponse(entry, ledger.computeRunningBalance(productId, entry.getId()))).toList();
        return new PagedResponse<>(entries, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private void validateMovementLocations(MovementType type, Location from, Location to) {
        switch (type) {
            case RECEIPT -> {
                if (from.getId() != VENDOR_LOCATION_ID || to.isVirtual()) throw new IllegalArgumentException("A receipt moves stock from Vendor to a physical location");
            }
            case DELIVERY -> {
                if (from.isVirtual() || to.getId() != CUSTOMER_LOCATION_ID) throw new IllegalArgumentException("A delivery moves stock from a physical location to Customer");
            }
            case TRANSFER -> {
                if (from.isVirtual() || to.isVirtual()) throw new IllegalArgumentException("A transfer requires two physical locations");
            }
            case ADJUSTMENT -> {
                if (from.isVirtual() == to.isVirtual()
                        || (from.isVirtual() && from.getId() != ADJUSTMENT_LOCATION_ID)
                        || (to.isVirtual() && to.getId() != ADJUSTMENT_LOCATION_ID)) {
                    throw new IllegalArgumentException("An adjustment requires one physical location and Inventory Adjustment");
                }
            }
        }
    }

    private Stock newStock(Product product, Location location) {
        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setLocation(location);
        stock.setQuantity(BigDecimal.ZERO);
        stock.setUpdatedAt(LocalDateTime.now());
        return stock;
    }

    private LedgerEntryDto toResponse(StockLedger entry, BigDecimal runningBalance) {
        Product product = entry.getProduct();
        Location from = entry.getLocationFrom();
        Location to = entry.getLocationTo();
        return new LedgerEntryDto(entry.getId(), product.getId(), product.getName(), product.getSku(),
                from.getId(), from.getName(), to.getId(), to.getName(), entry.getMovementType(),
                entry.getReferenceDocType(), entry.getReferenceDocId(), entry.getQuantity(), entry.getStatus(),
                runningBalance, "on_time", entry.getCreatedBy(), entry.getCreatedAt());
    }
}