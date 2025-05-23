package com.core.banking.repository;

import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.enums.LoanRepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepaymentScheduleRepository extends JpaRepository<LoanRepaymentSchedule, String> {
    Optional<LoanRepaymentSchedule> findFirstByLoanAccountIdAndPaymentStatusOrderByInstallmentNumberAsc(
            Long loanAccountId, LoanRepaymentStatus status);
}
