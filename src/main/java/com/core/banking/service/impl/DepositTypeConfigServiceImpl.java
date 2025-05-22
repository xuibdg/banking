package com.core.banking.service.impl;

import com.core.banking.entity.DepositTypeConfig;
import com.core.banking.repository.DepositTypeConfigRepository;
import com.core.banking.service.DepositTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositTypeConfigServiceImpl implements DepositTypeConfigService {
    @Autowired
    private DepositTypeConfigRepository depositTypeConfigRepository;

    @Override
    public List<DepositTypeConfig> findAll() {
        return depositTypeConfigRepository.findAll();
    }
}
