package com.core.banking.repository;

import com.core.banking.entity.SavingType;
import com.core.banking.enums.SavingTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingTypeRepository extends JpaRepository<SavingType, String> {
    boolean existsByTypeName(SavingTypeStatus typeName);
}
