package com.stockroom.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationRequestDto(@JsonProperty("warehouse_id") Long warehouseId,
                                 @NotBlank(message = "Location name is required") String name,
                                 @NotBlank(message = "Location code is required") String code,
                                 @JsonProperty("is_internal") Boolean isInternal,
                                 @JsonProperty("is_virtual") Boolean isVirtual) {
}