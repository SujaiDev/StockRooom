package com.inventorymanagement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventorymanagement.dto.PagedResponse;
import com.inventorymanagement.dto.ProductRequestDto;
import com.inventorymanagement.dto.ProductResponseDto;
import com.inventorymanagement.dto.StockByLocationDto;
import com.inventorymanagement.exception.ConflictException;
import com.inventorymanagement.exception.ResourceNotFoundException;
import com.inventorymanagement.model.Category;
import com.inventorymanagement.model.Product;
import com.inventorymanagement.model.Stock;
import com.inventorymanagement.repository.CategoryRepository;
import com.inventorymanagement.repository.ProductRepository;
import com.inventorymanagement.repository.StockRepository;

@Service
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final StockRepository stocks;

    public ProductService(ProductRepository products, CategoryRepository categories, StockRepository stocks) {
        this.products = products;
        this.categories = categories;
        this.stocks = stocks;
    }

    @Transactional
    public ProductResponseDto create(ProductRequestDto request) {
        String sku = request.sku().trim();
        if (products.existsBySkuIgnoreCaseAndDeletedAtIsNull(sku)) {
            throw new ConflictException("Product SKU already exists: " + sku);
        }
        Product product = new Product();
        apply(product, request);
        product.setSku(sku);
        product.setCreatedAt(LocalDateTime.now());
        return toResponse(products.save(product), BigDecimal.ZERO, List.of());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseDto> list(String search, Long categoryId, Boolean lowStock,
                                                   Long warehouseId, Pageable pageable) {
        Page<Product> page;
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        if (warehouseId != null) {
            page = products.findByWarehouse(warehouseId, normalizedSearch, categoryId, pageable);
        } else if (Boolean.TRUE.equals(lowStock)) {
            page = products.findLowStockProducts(categoryId, pageable);
        } else {
            page = products.searchProducts(normalizedSearch, categoryId, pageable);
        }
        return new PagedResponse<>(page.getContent().stream()
                .map(product -> toResponse(product, stocks.sumQuantityByProductId(product.getId()), null)).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductResponseDto get(Long id) {
        Product product = getActive(id);
        List<StockByLocationDto> stockByLocation = stocks.findByProductId(id).stream()
                .filter(stock -> !stock.getLocation().isVirtual())
                .map(this::toStockLocation).toList();
        return toResponse(product, stocks.sumQuantityByProductId(id), stockByLocation);
    }

    @Transactional
    public ProductResponseDto update(Long id, ProductRequestDto request) {
        Product product = getActive(id);
        String sku = request.sku().trim();
        if (!product.getSku().equalsIgnoreCase(sku)
                && products.existsBySkuIgnoreCaseAndDeletedAtIsNull(sku)) {
            throw new ConflictException("Product SKU already exists: " + sku);
        }
        apply(product, request);
        product.setSku(sku);
        return toResponse(products.save(product), stocks.sumQuantityByProductId(id), null);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getActive(id);
        product.setDeletedAt(LocalDateTime.now());
        products.save(product);
    }

    @Transactional(readOnly = true)
    public Product getActive(Long id) {
        return products.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private void apply(Product product, ProductRequestDto request) {
        product.setName(request.name().trim());
        product.setUnitOfMeasure(request.unitOfMeasure());
        product.setPerUnitCost(request.perUnitCost());
        product.setReorderPoint(request.reorderPoint() == null ? 0 : request.reorderPoint());
        product.setReorderQty(request.reorderQty() == null ? 0 : request.reorderQty());
        if (request.categoryId() == null) {
            product.setCategory(null);
        } else {
            product.setCategory(categories.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId())));
        }
    }

    private ProductResponseDto toResponse(Product product, BigDecimal onHand, List<StockByLocationDto> stockByLocation) {
        Category category = product.getCategory();
        return new ProductResponseDto(product.getId(), product.getName(), product.getSku(),
                category == null ? null : category.getId(), category == null ? null : category.getName(),
                product.getUnitOfMeasure(), product.getPerUnitCost(), product.getReorderPoint(),
                product.getReorderQty(), onHand == null ? BigDecimal.ZERO : onHand, stockByLocation,
                product.getCreatedAt());
    }

    private StockByLocationDto toStockLocation(Stock stock) {
        var location = stock.getLocation();
        var warehouse = location.getWarehouse();
        return new StockByLocationDto(location.getId(), location.getName(), location.getCode(),
                warehouse == null ? null : warehouse.getId(), warehouse == null ? null : warehouse.getName(),
                stock.getQuantity(), BigDecimal.ZERO);
    }
}