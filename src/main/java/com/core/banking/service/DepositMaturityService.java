package com.core.banking.service;

import com.core.banking.dto.DepositMaturityResponse;

import java.time.LocalDate;
import java.util.List;

public interface DepositMaturityService {
    DepositMaturityResponse processMaturity(Long depositoAccountId, String savingAccountId);

    List<DepositMaturityResponse> getAllMaturedDeposits(LocalDate maturityDate);
}
