package com.core.banking.repository;

import com.core.banking.entity.SavingTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingTypeConfigRepository extends JpaRepository<SavingTypeConfig, Long> {
}
