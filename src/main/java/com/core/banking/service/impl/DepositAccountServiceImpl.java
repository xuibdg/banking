package com.core.banking.service.impl;

import com.core.banking.entity.DepositAccount;
import com.core.banking.repository.DepositAccountRepository;
import com.core.banking.service.DepositAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositAccountServiceImpl implements DepositAccountService {
    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Override
    public List<DepositAccount> findAll() {
        return depositAccountRepository.findAll();
    }
}
