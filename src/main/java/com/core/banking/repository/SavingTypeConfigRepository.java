package com.core.banking.repository;

import com.core.banking.entity.SavingConfiguration;
import com.core.banking.entity.SavingType;
import com.core.banking.entity.SavingTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavingTypeConfigRepository extends JpaRepository<SavingTypeConfig, String> {
    Optional<SavingTypeConfig> findBySavingType(SavingType savingType);

}
