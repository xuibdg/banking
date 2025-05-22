package com.core.banking.service;

import com.core.banking.dto.SavingTypeRequest;
import com.core.banking.dto.SavingTypeResponse;
import com.core.banking.entity.SavingTypeConfig;
import java.util.List;

public interface SavingTypeConfigService {

    List<SavingTypeConfig> findAll();
    SavingTypeResponse createOrUpdateConfiguration(SavingTypeRequest request);
    List<SavingTypeResponse> getAllConfigurations();
    SavingTypeResponse getConfigurationById(Long id);
    SavingTypeResponse updateConfiguration(Long id, SavingTypeRequest request);
}
