package com.core.banking.service;

import com.core.banking.entity.LoanRepaymentSchedule;
import java.util.List;

public interface LoanRepaymentScheduleService {
    List<LoanRepaymentSchedule> findAll();
}
