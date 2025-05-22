package com.core.banking.repository;

import com.core.banking.entity.DepositAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositAccountRepository extends JpaRepository<DepositAccount, String> {
}
