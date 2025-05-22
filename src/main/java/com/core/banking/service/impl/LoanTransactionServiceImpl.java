package com.core.banking.service.impl;

import com.core.banking.entity.LoanTransaction;
import com.core.banking.repository.LoanTransactionRepository;
import com.core.banking.service.LoanTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LoanTransactionServiceImpl implements LoanTransactionService {
    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Override
    public List<LoanTransaction> findAll() {
        return loanTransactionRepository.findAll();
    }
}
