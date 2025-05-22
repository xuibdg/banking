package com.core.banking.controller;

import com.core.banking.entity.LoanAccount;
import com.core.banking.service.LoanAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/loan-accounts")
public class LoanAccountController {
    @Autowired
    private LoanAccountService loanAccountService;

    @GetMapping
    public List<LoanAccount> getAll() {
        return loanAccountService.findAll();
    }
}
