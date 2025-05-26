package com.core.banking.service;

import com.core.banking.dto.SavingConfResponse;
import com.core.banking.dto.SavingTypeConfRequest;
import com.core.banking.entity.SavingTypeConfig;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SavingTypeConfigService {

    SavingTypeConfig createSavingTypeConfig(SavingTypeConfRequest config);
    List<SavingTypeConfig> getAllConfigs();
    Optional<SavingTypeConfig> getConfigById(String id);
    List<SavingTypeConfig> getConfigsBySavingTypeId(String savingTypeId);
    List<SavingTypeConfig> getActiveConfigs();
    SavingTypeConfig updateSavingTypeConfig(String id, SavingTypeConfRequest updatedConfig);
    String deleteSavingType(String id);

}
