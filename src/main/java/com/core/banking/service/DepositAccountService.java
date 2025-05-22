package com.core.banking.service;

import com.core.banking.entity.DepositoAccount;
import java.util.List;

public interface DepositoAccountService {
    List<DepositoAccount> findAll();
}
