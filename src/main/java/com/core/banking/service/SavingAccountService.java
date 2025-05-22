package com.core.banking.service;

import com.core.banking.entity.SavingAccount;
import java.util.List;

public interface SavingAccountService {
    List<SavingAccount> findAll();
}
