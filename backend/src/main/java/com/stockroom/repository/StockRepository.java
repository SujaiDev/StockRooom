package com.stockroom.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stockroom.model.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.product.id = :productId and s.location.id = :locationId")
    Optional<Stock> findByProductIdAndLocationIdForUpdate(@Param("productId") Long productId,
                                                          @Param("locationId") Long locationId);

    List<Stock> findByProductId(Long productId);

    @Query("select coalesce(sum(s.quantity), 0) from Stock s where s.product.id = :productId and s.location.virtual = false")
    BigDecimal sumQuantityByProductId(@Param("productId") Long productId);

    @Query("select coalesce(sum(s.quantity), 0) from Stock s where s.location.virtual = false")
    BigDecimal sumAllPhysicalQuantity();

    @Query("select coalesce(sum(s.quantity * s.product.perUnitCost), 0) from Stock s "
            + "where s.location.virtual = false and s.product.deletedAt is null")
    BigDecimal sumInventoryValue();
}