package com.inventorymanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventorymanagement.dto.ApiResponse;
import com.inventorymanagement.dto.LocationRequestDto;
import com.inventorymanagement.dto.LocationResponseDto;
import com.inventorymanagement.dto.WarehouseRequestDto;
import com.inventorymanagement.dto.WarehouseResponseDto;
import com.inventorymanagement.service.WarehouseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class WarehouseController {

    private final WarehouseService warehouses;

    public WarehouseController(WarehouseService warehouses) {
        this.warehouses = warehouses;
    }

    @GetMapping("/warehouses")
    public ApiResponse<List<WarehouseResponseDto>> list() {
        return ApiResponse.ok(warehouses.list());
    }

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<WarehouseResponseDto>> create(@Valid @RequestBody WarehouseRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(warehouses.create(request)));
    }

    @GetMapping("/warehouses/{id}")
    public ApiResponse<WarehouseResponseDto> get(@PathVariable Long id) {
        return ApiResponse.ok(warehouses.get(id));
    }

    @PostMapping("/warehouses/{id}/locations")
    public ResponseEntity<ApiResponse<LocationResponseDto>> createForWarehouse(@PathVariable Long id,
            @Valid @RequestBody LocationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(warehouses.createLocation(request, id)));
    }

    @GetMapping("/locations")
    public ApiResponse<List<LocationResponseDto>> locations(@RequestParam(name = "warehouse_id", required = false) Long warehouseId) {
        return ApiResponse.ok(warehouses.listLocations(warehouseId));
    }

    @PostMapping("/locations")
    public ResponseEntity<ApiResponse<LocationResponseDto>> createLocation(@Valid @RequestBody LocationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(warehouses.createLocation(request, null)));
    }
}