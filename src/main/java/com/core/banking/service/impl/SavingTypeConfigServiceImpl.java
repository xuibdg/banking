package com.core.banking.service.impl;

import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.repository.SavingTypeConfigRepository;
import com.core.banking.service.SavingTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SavingTypeConfigServiceImpl implements SavingTypeConfigService {
    @Autowired
    private SavingTypeConfigRepository savingTypeConfigRepository;

    @Override
    public List<SavingTypeConfig> findAll() {
        return savingTypeConfigRepository.findAll();
    }
}
