package com.core.banking.repository;

import com.core.banking.entity.SavingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingTypeRepository extends JpaRepository<SavingType, Long> {
}
