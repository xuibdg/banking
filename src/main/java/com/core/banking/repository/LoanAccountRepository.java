package com.core.banking.repository;

import com.core.banking.entity.Customer;
import com.core.banking.entity.LoanAccount;
import com.core.banking.enums.LoanAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, String> {
    Optional<LoanAccount> findById(String loanAccountId);
    boolean existsByCustomerIdAndAccountStatusIn(Customer customer, List<LoanAccountStatus> statuses);
}

