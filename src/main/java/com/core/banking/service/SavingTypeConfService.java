package com.core.banking.service;

import com.core.banking.dto.SavingConfRequest;
import com.core.banking.dto.SavingConfResponse;

import java.util.List;

public interface SavingTypeConfService {

    SavingConfResponse createOrUpdateConfiguration(SavingConfRequest request);
    List<SavingConfResponse> getAllConfigurations();
    SavingConfResponse getConfigurationById(Long id);
    SavingConfResponse updateConfiguration(Long id, SavingConfRequest request);

}
