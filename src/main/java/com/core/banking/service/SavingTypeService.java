package com.core.banking.service;

import com.core.banking.dto.SavingTypeRequest;
import com.core.banking.entity.SavingType;

import java.util.List;
import java.util.Optional;

public interface SavingTypeService {

    List<SavingType> findAll();
    SavingType createSavingType(SavingTypeRequest savingType);
    List<SavingType> getAllSavingTypes();
    Optional<SavingType> getSavingTypeById(String id);
    SavingType updateSavingType(String savingTypeId, SavingTypeRequest updatedSavingType);
    String deleteSavingType(String id);
}
