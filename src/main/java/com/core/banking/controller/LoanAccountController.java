package com.core.banking.controller;

import com.core.banking.dto.LoanAccountRequest;
import com.core.banking.entity.LoanAccount;
import com.core.banking.service.LoanAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/loan-accounts")
public class LoanAccountController {
    @Autowired
    private LoanAccountService loanAccountService;

    @GetMapping
    public List<LoanAccount> getAll() {
        return loanAccountService.findAll();
    }

    @PostMapping("/create")
    public String createLoanAccount(@RequestBody LoanAccountRequest request) {
        loanAccountService.createLoanAccount(request);
        return "Loan account created successfully.";
    }

    @PostMapping("/{loanAccountId}/approve-disburse")
    public String approveAndDisburseLoan(@PathVariable String loanAccountId) {
        loanAccountService.ApproveAndDisburseLoan(loanAccountId);
        return "Loan berhasil di-approve dan didisburse.";
    }

}
