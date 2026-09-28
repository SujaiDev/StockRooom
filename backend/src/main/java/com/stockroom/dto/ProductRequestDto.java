package com.stockroom.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequestDto(
        @NotBlank(message = "Product name is required") String name,
        @NotBlank(message = "SKU is required") String sku,
        @JsonProperty("category_id") Long categoryId,
        @JsonProperty("unit_of_measure") String unitOfMeasure,
        @JsonProperty("per_unit_cost") @DecimalMin(value = "0.0", message = "Unit cost cannot be negative") BigDecimal perUnitCost,
        @JsonProperty("reorder_point") @PositiveOrZero(message = "Reorder point cannot be negative") Integer reorderPoint,
        @JsonProperty("reorder_qty") @PositiveOrZero(message = "Reorder quantity cannot be negative") Integer reorderQty) {
}