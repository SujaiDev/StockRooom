package com.inventorymanagement.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inventorymanagement.model.MovementType;
import com.inventorymanagement.model.StockLedger;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
    Page<StockLedger> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    @Query("""
        select coalesce(sum(case
          when sl.locationTo.virtual = false and sl.locationFrom.virtual = true then sl.quantity
          when sl.locationTo.virtual = true and sl.locationFrom.virtual = false then -sl.quantity
          else 0 end), 0)
        from StockLedger sl
        where sl.product.id = :productId and sl.id <= :targetEntryId
    """)
    BigDecimal computeRunningBalance(@Param("productId") Long productId, @Param("targetEntryId") Long targetEntryId);

    @Query("""
        select sl from StockLedger sl
        where (:productId is null or sl.product.id = :productId)
          and (:locationId is null or sl.locationFrom.id = :locationId or sl.locationTo.id = :locationId)
          and (:movementType is null or sl.movementType = :movementType)
          and (:fromDate is null or sl.createdAt >= :fromDate)
          and (:toDate is null or sl.createdAt <= :toDate)
          and (:search is null or lower(sl.product.name) like lower(concat('%', :search, '%'))
               or lower(sl.product.sku) like lower(concat('%', :search, '%')))
        order by sl.createdAt desc, sl.id desc
    """)
    Page<StockLedger> findWithFilters(@Param("productId") Long productId, @Param("locationId") Long locationId,
                                      @Param("movementType") MovementType movementType,
                                      @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate,
                                      @Param("search") String search, Pageable pageable);
}