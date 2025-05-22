package com.core.banking.service;

import com.core.banking.entity.DepositTypeConfig;
import java.util.List;

public interface DepositTypeConfigService {
    List<DepositTypeConfig> findAll();
}
