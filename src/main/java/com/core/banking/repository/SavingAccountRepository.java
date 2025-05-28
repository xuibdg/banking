package com.core.banking.repository;

import com.core.banking.entity.Customer;
import com.core.banking.entity.SavingAccount;
import com.core.banking.enums.SavingAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, String> {
    boolean existsByCustomer_IdAndAccountStatus(String customerId, SavingAccountStatus accountStatus);
}
