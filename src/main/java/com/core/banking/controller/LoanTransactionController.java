package com.core.banking.controller;

import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.entity.LoanTransaction;
import com.core.banking.service.LoanTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-transactions")
public class LoanTransactionController {
    @Autowired
    private LoanTransactionService loanTransactionService;

    @GetMapping
    public List<LoanTransaction> getAll() {
        return loanTransactionService.findAll();
    }

    @PostMapping("/create")
    public String createLoanTransaction (@RequestBody LoanTransactionRequest request) {
        return loanTransactionService.createLoanTransaction(request);
    }

    @PutMapping("/{loanTransactionId}")
    public String updateLoanTransaction (@PathVariable String loanTransactionId, @RequestBody LoanTransactionRequest request) {
        return loanTransactionService.updateLoanTransaction(loanTransactionId,request);
    }

    @DeleteMapping("/{loanTransactionId}")
    public String deleteLoanTransaction (@PathVariable String loanTransactionId) {
        return loanTransactionService.deleteLoanTransaction(loanTransactionId);
    }

}
