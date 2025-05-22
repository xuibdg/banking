package com.core.banking.repository;

import com.core.banking.entity.SavingConfiguration;
import com.core.banking.enums.SavingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavingConfigurationRepository extends JpaRepository<SavingConfiguration,Long> {
    Optional<SavingConfiguration> findBySavingType(SavingType savingType);
}
