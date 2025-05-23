package com.core.banking.service;

import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.entity.LoanTransaction;
import java.util.List;

public interface LoanTransactionService {
    String createLoanTransaction (LoanTransactionRequest request);
    List<LoanTransaction> findAll();
    String updateLoanTransaction (String loanTransactionId, LoanTransactionRequest request);
    String deleteLoanTransaction (String loanTransactionId);
}
