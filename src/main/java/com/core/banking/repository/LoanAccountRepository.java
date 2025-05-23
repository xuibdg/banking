package com.core.banking.repository;

import com.core.banking.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, String> {
    Optional<LoanAccount> findById(String loanAccountId);
}

