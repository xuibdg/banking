package com.core.banking.service;

import com.core.banking.entity.SavingTypeConfig;
import java.util.List;

public interface SavingTypeConfigService {
    List<SavingTypeConfig> findAll();
}
