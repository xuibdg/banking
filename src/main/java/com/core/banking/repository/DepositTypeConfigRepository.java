package com.core.banking.repository;

import com.core.banking.entity.DepositTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositTypeConfigRepository extends JpaRepository<DepositTypeConfig, String> {
}
