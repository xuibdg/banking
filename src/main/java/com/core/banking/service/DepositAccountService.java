package com.core.banking.service;

import com.core.banking.dto.DepositAccountRequest;
import com.core.banking.dto.DepositAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.DepositAccount;
import com.core.banking.enums.DepositAccountStatus;

import java.util.List;

public interface DepositAccountService {
    List<DepositAccount> findAll();

    DepositAccountResponse openDepositAccount(DepositAccountRequest depositAccountRequest, UserMetaData userMetaData);

    DepositAccountResponse getDepositAccountById(Long depositAccountId);

    List<DepositAccountResponse> getDepositAccountsByCustomerId(String customerId);

    List<DepositAccountResponse> getDepositAccountsByStatus(DepositAccountStatus status);
}
