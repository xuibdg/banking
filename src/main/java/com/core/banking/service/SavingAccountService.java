package com.core.banking.service;

import com.core.banking.dto.SavingAccountRequest;
import com.core.banking.dto.SavingAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.SavingAccount;
import com.core.banking.enums.SavingAccountStatus;

import java.util.List;

public interface SavingAccountService {
    List<SavingAccount> findAll();
    String create(SavingAccountRequest request, UserMetaData userMetaData);
    SavingAccountResponse getByAccountNumber(String accountNumber);
    SavingAccountResponse updateStatus(String id, SavingAccountStatus status);
    List<SavingAccountResponse> getAll();
}