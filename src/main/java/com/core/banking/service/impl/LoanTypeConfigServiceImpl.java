package com.core.banking.service.impl;

import com.core.banking.entity.LoanTypeConfig;
import com.core.banking.repository.LoanTypeConfigRepository;
import com.core.banking.service.LoanTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LoanTypeConfigServiceImpl implements LoanTypeConfigService {
    @Autowired
    private LoanTypeConfigRepository loanTypeConfigRepository;

    @Override
    public List<LoanTypeConfig> findAll() {
        return loanTypeConfigRepository.findAll();
    }
}
