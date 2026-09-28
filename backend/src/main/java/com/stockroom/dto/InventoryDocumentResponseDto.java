package com.stockroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockroom.model.DocumentStatus;
import com.stockroom.model.DocumentType;

public record InventoryDocumentResponseDto(Long id,
                                          @JsonProperty("document_number") String documentNumber,
                                          @JsonProperty("document_type") DocumentType documentType,
                                          DocumentStatus status, String note,
                                          @JsonProperty("created_by") String createdBy,
                                          @JsonProperty("created_at") LocalDateTime createdAt,
                                          @JsonProperty("updated_at") LocalDateTime updatedAt,
                                          List<Line> lines) {
    public record Line(Long id, @JsonProperty("line_number") int lineNumber,
                       @JsonProperty("product_id") Long productId,
                       @JsonProperty("product_name") String productName,
                       @JsonProperty("product_sku") String productSku,
                       @JsonProperty("location_from_id") Long locationFromId,
                       @JsonProperty("location_from_name") String locationFromName,
                       @JsonProperty("location_to_id") Long locationToId,
                       @JsonProperty("location_to_name") String locationToName,
                       BigDecimal quantity) {
    }
}