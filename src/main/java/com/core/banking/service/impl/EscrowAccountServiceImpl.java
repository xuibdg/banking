package com.core.banking.service.impl;

import com.core.banking.entity.EscrowAccount;
import com.core.banking.repository.EscrowAccountRepository;
import com.core.banking.service.EscrowAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EscrowAccountServiceImpl implements EscrowAccountService {
    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Override
    public List<EscrowAccount> findAll() {
        return escrowAccountRepository.findAll();
    }
}
