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
        return loanAccountService.createLoanAccount(request);
    }

    @PutMapping("/{loanAccountId}")
    public String updateLoanAccount (@PathVariable String loanAccountId,@RequestBody LoanAccountRequest request) {
        return loanAccountService.updateLoanAccount(loanAccountId,request);
    }

    @DeleteMapping("/{loanAccountId}")
    public String deleteLoanAccount (@PathVariable String loanAccountId) {
        return loanAccountService.deleteLoanAccount(loanAccountId);
    }

}
