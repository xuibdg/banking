package com.core.banking.service;

import com.core.banking.entity.EscrowAccount;
import java.util.List;

public interface EscrowAccountService {
    List<EscrowAccount> findAll();
}
