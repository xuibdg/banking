package com.core.banking.service.impl;

import com.core.banking.entity.LoanAccount;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.service.LoanAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

public class LoanAccountDetail implements LoanAccountDetailService {


    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Override
    public String approveAccount(String loanAccountId) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new RuntimeException("Loan account tidak ditemukan"));

        loanAccount.setAccountStatus(LoanAccountStatus.APPROVED);
        loanAccount.setUpdatedAt(OffsetDateTime.now());

        loanAccountRepository.save(loanAccount);
        return "Loan account dengan ID " + loanAccountId + " berhasil di-approve.";
    }

}
