package com.inventorymanagement.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inventorymanagement.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
  long countByDeletedAtIsNull();

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    Optional<Product> findBySkuIgnoreCaseAndDeletedAtIsNull(String sku);

    boolean existsBySkuIgnoreCaseAndDeletedAtIsNull(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.deletedAt is null")
    Optional<Product> findActiveByIdForUpdate(@Param("id") Long id);

    @Query("""
        select p from Product p
        where p.deletedAt is null
          and (:categoryId is null or p.category.id = :categoryId)
          and (:search is null or lower(p.name) like lower(concat('%', :search, '%'))
               or lower(p.sku) like lower(concat('%', :search, '%')))
    """)
    Page<Product> searchProducts(@Param("search") String search, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
        select p from Product p
        where p.deletedAt is null
          and (:categoryId is null or p.category.id = :categoryId)
          and coalesce((select sum(s.quantity) from Stock s
                        where s.product.id = p.id and s.location.virtual = false), 0) <= p.reorderPoint
    """)
    Page<Product> findLowStockProducts(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
        select distinct p from Product p join Stock s on s.product.id = p.id
        where p.deletedAt is null and s.location.warehouse.id = :warehouseId
          and (:categoryId is null or p.category.id = :categoryId)
          and (:search is null or lower(p.name) like lower(concat('%', :search, '%'))
               or lower(p.sku) like lower(concat('%', :search, '%')))
    """)
    Page<Product> findByWarehouse(@Param("warehouseId") Long warehouseId, @Param("search") String search,
                                  @Param("categoryId") Long categoryId, Pageable pageable);
}