package com.core.banking.controller;

import com.core.banking.entity.DepositAccount;
import com.core.banking.service.DepositAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposit-accounts")
public class DepositAccountController {
    @Autowired
    private DepositAccountService depositAccountService;

    @GetMapping
    public List<DepositAccount> getAll() {
        return depositAccountService.findAll();
    }
}
