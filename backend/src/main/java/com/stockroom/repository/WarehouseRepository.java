package com.stockroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stockroom.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCodeIgnoreCase(String code);
}