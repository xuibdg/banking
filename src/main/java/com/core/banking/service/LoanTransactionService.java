package com.core.banking.service;

import com.core.banking.entity.LoanTransaction;
import java.util.List;

public interface LoanTransactionService {
    List<LoanTransaction> findAll();
}
