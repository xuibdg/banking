package com.core.banking.repository;

import com.core.banking.entity.LoanRepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepaymentScheduleRepository extends JpaRepository<LoanRepaymentSchedule, String> {
}
