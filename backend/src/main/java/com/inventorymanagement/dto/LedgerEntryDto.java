package com.inventorymanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventorymanagement.model.MovementType;

public record LedgerEntryDto(Long id,
                             @JsonProperty("product_id") Long productId,
                             @JsonProperty("product_name") String productName,
                             @JsonProperty("product_sku") String productSku,
                             @JsonProperty("location_from_id") Long locationFromId,
                             @JsonProperty("location_from_name") String locationFromName,
                             @JsonProperty("location_to_id") Long locationToId,
                             @JsonProperty("location_to_name") String locationToName,
                             @JsonProperty("movement_type") MovementType movementType,
                             @JsonProperty("reference_doc_type") String referenceDocType,
                             @JsonProperty("reference_doc_id") Long referenceDocId,
                             BigDecimal quantity, String status,
                             @JsonProperty("running_balance") BigDecimal runningBalance,
                             @JsonProperty("document_state") String documentState,
                             @JsonProperty("created_by") String createdBy,
                             @JsonProperty("created_at") LocalDateTime createdAt) {
}