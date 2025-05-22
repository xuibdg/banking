package com.core.banking.service.impl;

import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.repository.LoanRepaymentScheduleRepository;
import com.core.banking.service.LoanRepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LoanRepaymentScheduleServiceImpl implements LoanRepaymentScheduleService {
    @Autowired
    private LoanRepaymentScheduleRepository loanRepaymentScheduleRepository;

    @Override
    public List<LoanRepaymentSchedule> findAll() {
        return loanRepaymentScheduleRepository.findAll();
    }
}
