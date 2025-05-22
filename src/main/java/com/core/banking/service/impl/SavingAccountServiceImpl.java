package com.core.banking.service.impl;

import com.core.banking.entity.SavingAccount;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.SavingAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SavingAccountServiceImpl implements SavingAccountService {
    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Override
    public List<SavingAccount> findAll() {
        return savingAccountRepository.findAll();
    }
}
