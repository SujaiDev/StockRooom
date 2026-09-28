package com.inventorymanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequestDto(@NotBlank(message = "Warehouse name is required") String name,
                                 @NotBlank(message = "Warehouse code is required") String code,
                                 String address) {
}