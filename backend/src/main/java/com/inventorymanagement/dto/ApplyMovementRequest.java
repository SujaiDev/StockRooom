package com.inventorymanagement.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventorymanagement.model.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ApplyMovementRequest(@JsonProperty("product_id") @NotNull(message = "Product is required") Long productId,
                                   @JsonProperty("location_from_id") Long locationFromId,
                                   @JsonProperty("location_to_id") Long locationToId,
                                   @NotNull(message = "Quantity is required")
                                   @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero") BigDecimal quantity,
                                   @JsonProperty("movement_type") @NotNull(message = "Movement type is required") MovementType movementType,
                                   @JsonProperty("reference_doc_type") String referenceDocType,
                                   @JsonProperty("reference_doc_id") Long referenceDocId,
                                   @JsonProperty("created_by") String createdBy) {
}