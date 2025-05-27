package com.core.banking.repository;

import com.core.banking.entity.Customer;
import com.core.banking.entity.LoanAccount;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.enums.SavingAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, String> {
    boolean existsByCustomer_IdAndAccountStatus(String customerId, LoanAccountStatus accountStatus);
}
