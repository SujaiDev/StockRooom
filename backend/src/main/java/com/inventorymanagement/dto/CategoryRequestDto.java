package com.inventorymanagement.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoryRequestDto(@NotBlank(message = "Category name is required") String name,
								 @JsonProperty("parent_id") Long parentId) {
}