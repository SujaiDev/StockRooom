package com.inventorymanagement.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public record StockByLocationDto(@JsonProperty("location_id") Long locationId,
                                 @JsonProperty("location_name") String locationName,
                                 @JsonProperty("location_code") String locationCode,
                                 @JsonProperty("warehouse_id") Long warehouseId,
                                 @JsonProperty("warehouse_name") String warehouseName,
                                 @JsonProperty("on_hand") BigDecimal onHand, BigDecimal reserved) {
}