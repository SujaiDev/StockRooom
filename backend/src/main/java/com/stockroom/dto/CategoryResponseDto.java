package com.stockroom.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoryResponseDto(Long id, String name,
								  @JsonProperty("parent_id") Long parentId,
								  @JsonProperty("parent_name") String parentName,
								  @JsonProperty("created_at") LocalDateTime createdAt) {
}