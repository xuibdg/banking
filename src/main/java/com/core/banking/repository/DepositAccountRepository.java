package com.core.banking.repository;

import com.core.banking.entity.DepositoAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoAccountRepository extends JpaRepository<DepositoAccount, Long> {
}
