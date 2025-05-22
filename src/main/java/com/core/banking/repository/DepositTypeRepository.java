package com.core.banking.repository;

import com.core.banking.entity.DepositoType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoTypeRepository extends JpaRepository<DepositoType, Long> {
}
