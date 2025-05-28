package com.core.banking.service;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanRepaymentScheduleRequest;
import com.core.banking.dto.LoanRepaymentScheduleResponse;

import java.util.List;

public interface LoanRepaymentScheduleService {
    List<LoanRepaymentScheduleResponse> findAll();
    String createLoanRepaymentSchedule (LoanRepaymentScheduleRequest request, UserMetaData userMetaData);
    LoanRepaymentScheduleResponse loanRepayment (LoanRepaymentScheduleRequest request, UserMetaData userMetaData);
    String updateLoanRepaymentSchedule (String loanRepaymentScheduleId, LoanRepaymentScheduleRequest request, UserMetaData userMetaData);
    String deleteLoanRepaymentSchedule (String loanRepaymentScheduleId, UserMetaData userMetaData);
}
