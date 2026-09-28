package com.inventorymanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventorymanagement.dto.ApplyMovementRequest;
import com.inventorymanagement.dto.InventoryDocumentRequestDto;
import com.inventorymanagement.dto.InventoryDocumentResponseDto;
import com.inventorymanagement.dto.PagedResponse;
import com.inventorymanagement.exception.ConflictException;
import com.inventorymanagement.exception.ResourceNotFoundException;
import com.inventorymanagement.model.DocumentStatus;
import com.inventorymanagement.model.DocumentType;
import com.inventorymanagement.model.InventoryDocument;
import com.inventorymanagement.model.InventoryDocumentLine;
import com.inventorymanagement.model.Location;
import com.inventorymanagement.model.MovementType;
import com.inventorymanagement.model.Product;
import com.inventorymanagement.repository.InventoryDocumentLineRepository;
import com.inventorymanagement.repository.InventoryDocumentRepository;
import com.inventorymanagement.repository.LocationRepository;
import com.inventorymanagement.repository.ProductRepository;

@Service
public class InventoryDocumentService {

    private static final long VENDOR_ID = 1L;
    private static final long CUSTOMER_ID = 2L;
    private static final long ADJUSTMENT_ID = 3L;

    private final InventoryDocumentRepository documents;
    private final InventoryDocumentLineRepository lines;
    private final ProductRepository products;
    private final LocationRepository locations;
    private final StockLedgerService stockLedger;

    public InventoryDocumentService(InventoryDocumentRepository documents, InventoryDocumentLineRepository lines,
                                    ProductRepository products, LocationRepository locations,
                                    StockLedgerService stockLedger) {
        this.documents = documents;
        this.lines = lines;
        this.products = products;
        this.locations = locations;
        this.stockLedger = stockLedger;
    }

    @Transactional
    public InventoryDocumentResponseDto create(DocumentType type, InventoryDocumentRequestDto request, String actor) {
        InventoryDocument document = new InventoryDocument();
        document.setDocumentType(type);
        document.setStatus(DocumentStatus.DRAFT);
        document.setDocumentNumber(type.name().substring(0, 3) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        document.setNote(request.note());
        document.setCreatedBy(normalizeActor(actor));
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(document.getCreatedAt());
        document = documents.save(document);

        int lineNumber = 1;
        for (InventoryDocumentRequestDto.Line requestedLine : request.lines()) {
            InventoryDocumentLine line = new InventoryDocumentLine();
            line.setDocument(document);
            line.setLineNumber(lineNumber++);
            line.setProduct(products.findByIdAndDeletedAtIsNull(requestedLine.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + requestedLine.productId())));
            Location from = findOptionalLocation(requestedLine.locationFromId(), "Source");
            Location to = findOptionalLocation(requestedLine.locationToId(), "Destination");
                NormalizedLocations normalized = normalizeAndValidateLocations(type, from, to);
                line.setLocationFrom(normalized.from());
                line.setLocationTo(normalized.to());
            line.setQuantity(requestedLine.quantity());
            lines.save(line);
        }
        return get(type, document.getId());
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryDocumentResponseDto> list(DocumentType type, Pageable pageable) {
        Page<InventoryDocument> page = documents.findByDocumentTypeOrderByCreatedAtDesc(type, pageable);
        return new PagedResponse<>(page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public InventoryDocumentResponseDto get(DocumentType type, Long id) {
        InventoryDocument document = documents.findById(id)
                .filter(found -> found.getDocumentType() == type)
                .orElseThrow(() -> new ResourceNotFoundException(type + " document not found: " + id));
        return toResponse(document);
    }

    @Transactional
    public InventoryDocumentResponseDto validate(DocumentType type, Long id, String actor) {
        InventoryDocument document = getForUpdate(type, id);
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new ConflictException("Only draft documents can be validated");
        }
        for (InventoryDocumentLine line : lines.findByDocumentIdOrderByLineNumber(id)) {
            stockLedger.applyMovement(new ApplyMovementRequest(line.getProduct().getId(),
                    line.getLocationFrom() == null ? null : line.getLocationFrom().getId(),
                    line.getLocationTo() == null ? null : line.getLocationTo().getId(), line.getQuantity(),
                    toMovementType(type), type.name(), id, actor), actor);
        }
        document.setStatus(DocumentStatus.DONE);
        document.setUpdatedAt(LocalDateTime.now());
        documents.save(document);
        return toResponse(document);
    }

    @Transactional
    public InventoryDocumentResponseDto cancel(DocumentType type, Long id) {
        InventoryDocument document = getForUpdate(type, id);
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new ConflictException("Only draft documents can be cancelled");
        }
        document.setStatus(DocumentStatus.CANCELLED);
        document.setUpdatedAt(LocalDateTime.now());
        return toResponse(documents.save(document));
    }

    private InventoryDocument getForUpdate(DocumentType type, Long id) {
        return documents.findWithLockingByIdAndDocumentType(id, type)
                .orElseThrow(() -> new ResourceNotFoundException(type + " document not found: " + id));
    }

    private Location findOptionalLocation(Long id, String label) {
        if (id == null) return null;
        return locations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " location not found: " + id));
    }

    private NormalizedLocations normalizeAndValidateLocations(DocumentType type, Location from, Location to) {
        switch (type) {
            case RECEIPT -> {
                if (from == null) from = locations.findById(VENDOR_ID).orElseThrow();
                if (to == null || from.getId() != VENDOR_ID || to.isVirtual()) {
                    throw new IllegalArgumentException("Receipt lines need a physical destination; source defaults to Vendor");
                }
            }
            case DELIVERY -> {
                if (to == null) to = locations.findById(CUSTOMER_ID).orElseThrow();
                if (from == null || from.isVirtual() || to.getId() != CUSTOMER_ID) {
                    throw new IllegalArgumentException("Delivery lines need a physical source; destination defaults to Customer");
                }
            }
            case TRANSFER -> {
                if (from == null || to == null || from.isVirtual() || to.isVirtual() || from.getId().equals(to.getId())) {
                    throw new IllegalArgumentException("Transfer lines need two different physical locations");
                }
            }
            case ADJUSTMENT -> {
                if (from == null && to != null) from = locations.findById(ADJUSTMENT_ID).orElseThrow();
                else if (to == null && from != null) to = locations.findById(ADJUSTMENT_ID).orElseThrow();
                if (from == null || to == null || from.isVirtual() == to.isVirtual() || from.getId().equals(to.getId())) {
                    throw new IllegalArgumentException("Adjustment lines need one physical location and a positive/negative quantity direction");
                }
                if ((from.isVirtual() && from.getId() != ADJUSTMENT_ID)
                        || (to.isVirtual() && to.getId() != ADJUSTMENT_ID)) {
                    throw new IllegalArgumentException("Adjustments must use the Inventory Adjustment virtual location");
                }
            }
        }
        return new NormalizedLocations(from, to);
    }

    private record NormalizedLocations(Location from, Location to) {
    }

    private MovementType toMovementType(DocumentType type) {
        return MovementType.valueOf(type.name());
    }

    private String normalizeActor(String actor) {
        return actor == null || actor.isBlank() ? "system" : actor;
    }

    private InventoryDocumentResponseDto toResponse(InventoryDocument document) {
        List<InventoryDocumentResponseDto.Line> lineResponses = lines.findByDocumentIdOrderByLineNumber(document.getId())
                .stream().map(line -> new InventoryDocumentResponseDto.Line(line.getId(), line.getLineNumber(),
                        line.getProduct().getId(), line.getProduct().getName(), line.getProduct().getSku(),
                        line.getLocationFrom() == null ? null : line.getLocationFrom().getId(),
                        line.getLocationFrom() == null ? null : line.getLocationFrom().getName(),
                        line.getLocationTo() == null ? null : line.getLocationTo().getId(),
                        line.getLocationTo() == null ? null : line.getLocationTo().getName(), line.getQuantity())).toList();
        return new InventoryDocumentResponseDto(document.getId(), document.getDocumentNumber(),
                document.getDocumentType(), document.getStatus(), document.getNote(), document.getCreatedBy(),
                document.getCreatedAt(), document.getUpdatedAt(), lineResponses);
    }
}