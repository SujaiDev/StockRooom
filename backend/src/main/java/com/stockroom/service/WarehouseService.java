package com.stockroom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockroom.dto.LocationRequestDto;
import com.stockroom.dto.LocationResponseDto;
import com.stockroom.dto.WarehouseRequestDto;
import com.stockroom.dto.WarehouseResponseDto;
import com.stockroom.exception.ConflictException;
import com.stockroom.exception.ResourceNotFoundException;
import com.stockroom.model.Location;
import com.stockroom.model.Warehouse;
import com.stockroom.repository.LocationRepository;
import com.stockroom.repository.WarehouseRepository;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouses;
    private final LocationRepository locations;

    public WarehouseService(WarehouseRepository warehouses, LocationRepository locations) {
        this.warehouses = warehouses;
        this.locations = locations;
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponseDto> list() {
        return warehouses.findAll().stream().map(warehouse -> toResponse(warehouse,
                locations.findByWarehouseId(warehouse.getId()).stream().map(this::toLocationResponse).toList())).toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponseDto get(Long id) {
        Warehouse warehouse = getWarehouse(id);
        return toResponse(warehouse, locations.findByWarehouseId(id).stream().map(this::toLocationResponse).toList());
    }

    @Transactional
    public WarehouseResponseDto create(WarehouseRequestDto request) {
        String code = request.code().trim();
        if (warehouses.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Warehouse code already exists: " + code);
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name().trim());
        warehouse.setCode(code);
        warehouse.setAddress(request.address());
        warehouse.setCreatedAt(LocalDateTime.now());
        return toResponse(warehouses.save(warehouse), List.of());
    }

    @Transactional
    public LocationResponseDto createLocation(LocationRequestDto request, Long pathWarehouseId) {
        Long warehouseId = pathWarehouseId == null ? request.warehouseId() : pathWarehouseId;
        if (pathWarehouseId != null && request.warehouseId() != null && !pathWarehouseId.equals(request.warehouseId())) {
            throw new IllegalArgumentException("Path warehouse ID does not match warehouse_id in request");
        }
        Warehouse warehouse = warehouseId == null ? null : getWarehouse(warehouseId);
        return saveLocation(request, warehouse);
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDto> listLocations(Long warehouseId) {
        List<Location> result = warehouseId == null ? locations.findAll() : locations.findByWarehouseId(warehouseId);
        return result.stream().map(this::toLocationResponse).toList();
    }

    private LocationResponseDto saveLocation(LocationRequestDto request, Warehouse warehouse) {
        String code = request.code().trim();
        if (locations.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Location code already exists: " + code);
        }
        Location location = new Location();
        location.setWarehouse(warehouse);
        location.setName(request.name().trim());
        location.setCode(code);
        location.setInternal(request.isInternal() == null || request.isInternal());
        location.setVirtual(Boolean.TRUE.equals(request.isVirtual()));
        if (location.isVirtual() && location.getWarehouse() != null) {
            throw new IllegalArgumentException("Virtual locations cannot belong to a warehouse");
        }
        if (location.isVirtual()) {
            throw new IllegalArgumentException("Virtual locations are system-managed");
        }
        location.setCreatedAt(LocalDateTime.now());
        return toLocationResponse(locations.save(location));
    }

    private Warehouse getWarehouse(Long id) {
        return warehouses.findById(id).orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + id));
    }

    private WarehouseResponseDto toResponse(Warehouse warehouse, List<LocationResponseDto> locationList) {
        return new WarehouseResponseDto(warehouse.getId(), warehouse.getName(), warehouse.getCode(),
                warehouse.getAddress(), locationList, warehouse.getCreatedAt());
    }

    private LocationResponseDto toLocationResponse(Location location) {
        Warehouse warehouse = location.getWarehouse();
        return new LocationResponseDto(location.getId(), warehouse == null ? null : warehouse.getId(),
                warehouse == null ? null : warehouse.getName(), location.getName(), location.getCode(),
                location.isInternal(), location.isVirtual(), location.getCreatedAt());
    }
}