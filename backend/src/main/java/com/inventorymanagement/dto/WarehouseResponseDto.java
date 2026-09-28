package com.inventorymanagement.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record WarehouseResponseDto(Long id, String name, String code, String address,
                                  List<LocationResponseDto> locations,
                                  @JsonProperty("created_at") LocalDateTime createdAt) {
}