package com.stockroom.exception;

import java.math.BigDecimal;
import java.util.Map;

public class InsufficientStockException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public InsufficientStockException(Long productId, Long locationId, BigDecimal available, BigDecimal requested) {
        super("Insufficient stock for product " + productId + " at location " + locationId);
        this.fieldErrors = Map.of(
                "product_id", productId.toString(),
                "location_id", locationId.toString(),
                "available", available.toPlainString(),
                "requested", requested.toPlainString());
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}