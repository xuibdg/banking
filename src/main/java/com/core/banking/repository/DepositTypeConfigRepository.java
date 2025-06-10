package com.core.banking.repository;

import com.core.banking.entity.DepositTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositTypeConfigRepository extends JpaRepository<DepositTypeConfig, Long> {
    List<DepositTypeConfig> findByIsActiveTrue();
}
