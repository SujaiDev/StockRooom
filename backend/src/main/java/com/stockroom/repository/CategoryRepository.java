package com.stockroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stockroom.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}