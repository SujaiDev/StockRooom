package com.inventorymanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventorymanagement.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByWarehouseId(Long warehouseId);

    Optional<Location> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}