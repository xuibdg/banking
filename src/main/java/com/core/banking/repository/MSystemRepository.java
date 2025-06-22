package com.core.banking.repository;

import com.core.banking.entity.MSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MSystemRepository extends JpaRepository<MSystem, String> {

    @Query("SELECT m from MSystem m")
    Optional<MSystem> findFirst();

    @Query(value = "select ms.system_at from m_system ms limit 1", nativeQuery = true)
    LocalDate findSystemAt();
}
