package com.inventorymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventorymanagement.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCodeIgnoreCase(String code);
}