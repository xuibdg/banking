package com.core.banking.controller;

import com.core.banking.entity.LoanTransaction;
import com.core.banking.service.LoanTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/loan-transactions")
public class LoanTransactionController {
    @Autowired
    private LoanTransactionService loanTransactionService;

    @GetMapping
    public List<LoanTransaction> getAll() {
        return loanTransactionService.findAll();
    }
}
