package com.core.banking.service;

import com.core.banking.dto.LoanAccountRequest;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanPaymentRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface LoanAccountService {
    // Tambahkan method sesuai kebutuhan
    List<LoanAccount> findAll();

    String createLoanAccount(LoanAccountRequest request);

    String updateLoanAccount (String loanAccountId, LoanAccountRequest request);

    String deleteLoanAccount (String loanAccountId);
}
