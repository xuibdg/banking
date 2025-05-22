package com.core.banking.service.impl;

import com.core.banking.entity.DepositoAccount;
import com.core.banking.repository.DepositoAccountRepository;
import com.core.banking.service.DepositoAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositoAccountServiceImpl implements DepositoAccountService {
    @Autowired
    private DepositoAccountRepository depositoAccountRepository;

    @Override
    public List<DepositoAccount> findAll() {
        return depositoAccountRepository.findAll();
    }
}
