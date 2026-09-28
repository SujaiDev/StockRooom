package com.inventorymanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventorymanagement.dto.ApiResponse;
import com.inventorymanagement.dto.CategoryRequestDto;
import com.inventorymanagement.dto.CategoryResponseDto;
import com.inventorymanagement.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categories;

    public CategoryController(CategoryService categories) {
        this.categories = categories;
    }

    @GetMapping
    public ApiResponse<List<CategoryResponseDto>> list() {
        return ApiResponse.ok(categories.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponseDto> get(@PathVariable Long id) {
        return ApiResponse.ok(categories.get(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> create(@Valid @RequestBody CategoryRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(categories.create(request)));
    }
}