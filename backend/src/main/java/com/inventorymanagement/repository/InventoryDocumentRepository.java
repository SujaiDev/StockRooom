package com.inventorymanagement.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inventorymanagement.model.DocumentStatus;
import com.inventorymanagement.model.DocumentType;
import com.inventorymanagement.model.InventoryDocument;
import jakarta.persistence.LockModeType;

public interface InventoryDocumentRepository extends JpaRepository<InventoryDocument, Long> {
    Page<InventoryDocument> findByDocumentTypeOrderByCreatedAtDesc(DocumentType documentType, Pageable pageable);

    Page<InventoryDocument> findByStatusOrderByCreatedAtDesc(DocumentStatus status, Pageable pageable);

    Page<InventoryDocument> findByDocumentTypeAndStatusOrderByCreatedAtDesc(DocumentType type, DocumentStatus status,
                                                                            Pageable pageable);

    long countByDocumentTypeAndStatus(DocumentType type, DocumentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from InventoryDocument d where d.id = :id and d.documentType = :type")
    Optional<InventoryDocument> findWithLockingByIdAndDocumentType(@Param("id") Long id,
                                                                   @Param("type") DocumentType documentType);
}