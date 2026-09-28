package com.stockroom.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PagedResponse<T>(List<T> items, int page, int limit, long total,
                               @JsonProperty("total_pages") int totalPages) {

    public PagedResponse(List<T> items, int page, int limit, long total) {
        this(items, page, limit, total, limit <= 0 ? 0 : (int) Math.ceil((double) total / limit));
    }
}