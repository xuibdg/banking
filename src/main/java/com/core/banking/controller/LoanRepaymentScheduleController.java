package com.core.banking.controller;

import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.service.LoanRepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/loan-repayment-schedules")
public class LoanRepaymentScheduleController {
    @Autowired
    private LoanRepaymentScheduleService loanRepaymentScheduleService;

    @GetMapping
    public List<LoanRepaymentSchedule> getAll() {
        return loanRepaymentScheduleService.findAll();
    }
}
