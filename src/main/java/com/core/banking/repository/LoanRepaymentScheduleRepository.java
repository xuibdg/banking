package com.core.banking.repository;

import com.core.banking.entity.LoanRepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


import java.util.Optional;

public interface LoanRepaymentScheduleRepository extends JpaRepository<LoanRepaymentSchedule, String> {
    Optional<LoanRepaymentSchedule> findByLoanAccount_LoanAccountIdAndInstallmentNumber(String loanAccountId, Integer installmentNumber);
}
