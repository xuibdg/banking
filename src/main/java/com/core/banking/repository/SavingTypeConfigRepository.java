package com.core.banking.repository;

import com.core.banking.entity.SavingType;
import com.core.banking.entity.SavingTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingTypeConfigRepository extends JpaRepository<SavingTypeConfig, String> {
    boolean existsBySavingType(SavingType savingType);
    List<SavingTypeConfig> findBySavingTypeSavingTypeId(String savingTypeId);
    List<SavingTypeConfig> findByIsActive(boolean isActive);



}
