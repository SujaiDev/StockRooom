package com.stockroom.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryDocumentRequestDto(String note,
                                          @NotNull @Size(min = 1, message = "At least one line is required")
                                          List<@Valid Line> lines) {
    public record Line(@JsonProperty("product_id") @NotNull Long productId,
                       @JsonProperty("location_from_id") Long locationFromId,
                       @JsonProperty("location_to_id") Long locationToId,
                       @NotNull @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero") BigDecimal quantity) {
    }
}