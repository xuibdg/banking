package com.core.banking.repository;

import com.core.banking.entity.SavingType;
import com.core.banking.enums.SavingTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavingTypeRepository extends JpaRepository<SavingType, String> {
    Optional<SavingType> findByTypeName(String typeName);
    boolean existsByTypeName(SavingTypeStatus typeName);
    @Modifying
    @Query(value = "DELETE FROM saving_type_configs WHERE saving_type_id = :savingTypeId", nativeQuery = true)
    void deleteConfigsBySavingTypeId(@Param("id") String savingTypeId);
}
