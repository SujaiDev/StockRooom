package com.stockroom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockroom.dto.CategoryRequestDto;
import com.stockroom.dto.CategoryResponseDto;
import com.stockroom.exception.ConflictException;
import com.stockroom.exception.ResourceNotFoundException;
import com.stockroom.model.Category;
import com.stockroom.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> list() {
        return categories.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto get(Long id) {
        return toResponse(categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)));
    }

    @Transactional
    public CategoryResponseDto create(CategoryRequestDto request) {
        String name = request.name().trim();
        if (categories.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category already exists: " + name);
        }
        Category category = new Category();
        category.setName(name);
        category.setCreatedAt(LocalDateTime.now());
        if (request.parentId() != null) {
            category.setParent(categories.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found: " + request.parentId())));
        }
        return toResponse(categories.save(category));
    }

    private CategoryResponseDto toResponse(Category category) {
        Category parent = category.getParent();
        return new CategoryResponseDto(category.getId(), category.getName(), parent == null ? null : parent.getId(),
                parent == null ? null : parent.getName(), category.getCreatedAt());
    }
}