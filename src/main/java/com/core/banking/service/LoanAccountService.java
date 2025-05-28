package com.core.banking.service;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanAccountRequest;
import com.core.banking.dto.LoanAccountResponse;

import java.util.List;

public interface LoanAccountService {
    List<LoanAccountResponse> findAll();
    String createLoanAccount(LoanAccountRequest request, UserMetaData userMetaData);
    String updateLoanAccount (String loanAccountId, LoanAccountRequest request, UserMetaData userMetaData);
    String deleteLoanAccount (String loanAccountId, UserMetaData userMetaData);
}
