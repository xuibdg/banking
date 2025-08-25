package com.core.banking.service;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.DepositAccountRequest;
import com.core.banking.dto.DepositAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.DepositAccount;
import com.core.banking.enums.DepositAccountStatus;

import java.util.List;
import java.util.Map;

public interface DepositAccountService {
    List<DepositAccount> findAll();

    DepositAccountResponse createDepositAccount(DepositAccountRequest depositAccountRequest, @CurrentUser UserMetaData userMetaData);

    DepositAccountResponse getDepositAccountById(Long depositAccountId);

    List<DepositAccountResponse> getDepositAccountsByCustomerId(String customerId);

    List<DepositAccountResponse> getDepositAccountsByStatus(DepositAccountStatus status);

    Map<String, Object> generateBilyet(Long depositAccountId, UserMetaData userMetaData);

//    Map<String, Object> validateBilyet(String bilyetNumber, String accountNumber);

//    String deleteDepositAccount(Long depositoAccountId);
}
