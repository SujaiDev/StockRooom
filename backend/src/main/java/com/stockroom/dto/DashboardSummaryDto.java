package com.stockroom.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DashboardSummaryDto(@JsonProperty("product_count") long productCount,
                                  @JsonProperty("units_on_hand") BigDecimal unitsOnHand,
                                  @JsonProperty("inventory_value") BigDecimal inventoryValue,
                                  @JsonProperty("low_stock_products") long lowStockProducts,
                                  @JsonProperty("draft_receipts") long draftReceipts,
                                  @JsonProperty("draft_deliveries") long draftDeliveries,
                                  @JsonProperty("draft_transfers") long draftTransfers,
                                  @JsonProperty("draft_adjustments") long draftAdjustments) {
}