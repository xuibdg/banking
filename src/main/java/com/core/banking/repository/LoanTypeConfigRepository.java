package com.core.banking.repository;

import com.core.banking.entity.LoanTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanTypeConfigRepository extends JpaRepository<LoanTypeConfig, Long> {
}
