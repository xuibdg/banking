package com.core.banking.service;

import com.core.banking.entity.DepositAccount;
import java.util.List;

public interface DepositAccountService {
    List<DepositAccount> findAll();
}
