package com.core.banking.service.impl;

import com.core.banking.entity.DepositoTypeConfig;
import com.core.banking.repository.DepositoTypeConfigRepository;
import com.core.banking.service.DepositoTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositoTypeConfigServiceImpl implements DepositoTypeConfigService {
    @Autowired
    private DepositoTypeConfigRepository depositoTypeConfigRepository;

    @Override
    public List<DepositoTypeConfig> findAll() {
        return depositoTypeConfigRepository.findAll();
    }
}
