package com.core.banking.repository;

import com.core.banking.entity.DepositType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositTypeRepository extends JpaRepository<DepositType, Long> {
}
