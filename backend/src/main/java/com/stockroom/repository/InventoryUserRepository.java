package com.stockroom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stockroom.model.InventoryUser;

public interface InventoryUserRepository extends JpaRepository<InventoryUser, Long> {
    Optional<InventoryUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}