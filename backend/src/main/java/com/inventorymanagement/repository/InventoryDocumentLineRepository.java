package com.inventorymanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventorymanagement.model.InventoryDocumentLine;

public interface InventoryDocumentLineRepository extends JpaRepository<InventoryDocumentLine, Long> {
    List<InventoryDocumentLine> findByDocumentIdOrderByLineNumber(Long documentId);
}