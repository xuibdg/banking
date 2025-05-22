package com.core.banking.service.impl;

import com.core.banking.entity.LoanAccount;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.service.LoanAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LoanAccountServiceImpl implements LoanAccountService {
    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Override
    public List<LoanAccount> findAll() {
        return loanAccountRepository.findAll();
    }
}
