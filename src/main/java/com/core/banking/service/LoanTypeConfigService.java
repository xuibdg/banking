package com.core.banking.service;

import com.core.banking.entity.LoanTypeConfig;
import java.util.List;

public interface LoanTypeConfigService {
    List<LoanTypeConfig> findAll();
}
