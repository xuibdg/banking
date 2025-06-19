package com.core.banking.repository;

import com.core.banking.entity.MSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MSystemRepository extends JpaRepository<MSystem, String> {

    @Query("SELECT m from MSystem m")
    Optional<MSystem> findFirst();
}
