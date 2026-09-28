package com.inventorymanagement.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationResponseDto(Long id,
                                  @JsonProperty("warehouse_id") Long warehouseId,
                                  @JsonProperty("warehouse_name") String warehouseName,
                                  String name, String code,
                                  @JsonProperty("is_internal") boolean isInternal,
                                  @JsonProperty("is_virtual") boolean isVirtual,
                                  @JsonProperty("created_at") LocalDateTime createdAt) {
}