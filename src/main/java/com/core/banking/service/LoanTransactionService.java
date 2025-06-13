package com.core.banking.service;

import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.dto.LoanTransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface LoanTransactionService {
    String createLoanTransaction (LoanTransactionRequest request, UserMetaData userMetaData);
    List<LoanTransactionResponse> findAll();
    LoanTransactionResponse approveAndDisburseLoan(String loanAccountId, UserMetaData userMetaData);
    String updateLoanTransaction (String loanTransactionId, LoanTransactionRequest request, UserMetaData userMetaData);
    String deleteLoanTransaction (String loanTransactionId, UserMetaData userMetaData);
}
