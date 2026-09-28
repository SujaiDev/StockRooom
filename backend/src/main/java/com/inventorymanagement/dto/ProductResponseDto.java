package com.inventorymanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductResponseDto(Long id, String name, String sku,
                                 @JsonProperty("category_id") Long categoryId,
                                 @JsonProperty("category_name") String categoryName,
                                 @JsonProperty("unit_of_measure") String unitOfMeasure,
                                 @JsonProperty("per_unit_cost") BigDecimal perUnitCost,
                                 @JsonProperty("reorder_point") Integer reorderPoint,
                                 @JsonProperty("reorder_qty") Integer reorderQty,
                                 @JsonProperty("on_hand") BigDecimal onHand,
                                 @JsonProperty("stock_by_location") List<StockByLocationDto> stockByLocation,
                                 @JsonProperty("created_at") LocalDateTime createdAt) {
}