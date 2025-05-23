package com.core.banking.controller;

import com.core.banking.dto.LoanRepaymentScheduleRequest;
import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.service.LoanRepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-repayment-schedules")
public class LoanRepaymentScheduleController {
    @Autowired
    private LoanRepaymentScheduleService loanRepaymentScheduleService;

    @GetMapping
    public List<LoanRepaymentSchedule> getAll() {
        return loanRepaymentScheduleService.findAll();
    }

    @PostMapping("/create")
    public String createLoanRepaymentSchedule (@RequestBody LoanRepaymentScheduleRequest request) {
        return loanRepaymentScheduleService.createLoanRepaymentSchedule(request);
    }

    @PutMapping("/{loanRepaymentScheduleId}")
    public String updateLoanRepaymentSchedule (@PathVariable String loanRepaymentScheduleId,@RequestBody LoanRepaymentScheduleRequest request) {
        return loanRepaymentScheduleService.updateLoanRepaymentSchedule(loanRepaymentScheduleId,request);
    }

    @DeleteMapping("/{loanRepaymentScheduleId}")
    public String deleteLoanRepaymentSchedule (String loanRepaymentScheduleId) {
        return loanRepaymentScheduleService.deleteLoanRepaymentSchedule(loanRepaymentScheduleId);
    }

}
