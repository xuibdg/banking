package com.core.banking.service;

import com.core.banking.dto.LoanRepaymentScheduleRequest;
import com.core.banking.entity.LoanRepaymentSchedule;
import java.util.List;

public interface LoanRepaymentScheduleService {
    List<LoanRepaymentSchedule> findAll();
    String createLoanRepaymentSchedule (LoanRepaymentScheduleRequest request);
    String updateLoanRepaymentSchedule (String loanRepaymentScheduleId, LoanRepaymentScheduleRequest request);
    String deleteLoanRepaymentSchedule (String loanRepaymentScheduleId);
}
