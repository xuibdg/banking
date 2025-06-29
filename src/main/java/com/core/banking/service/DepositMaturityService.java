package com.core.banking.service;

import com.core.banking.dto.DepositMaturityResponse;
import com.core.banking.dto.UserMetaData;

import java.time.LocalDate;
import java.util.List;

public interface DepositMaturityService {
    DepositMaturityResponse processMaturity(Long depositoAccountId, UserMetaData userMetaData);

    List<DepositMaturityResponse> getAllMaturedDeposits(LocalDate maturityDate);
}
