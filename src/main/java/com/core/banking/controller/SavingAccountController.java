package com.core.banking.controller;

import com.core.banking.entity.SavingAccount;
import com.core.banking.service.SavingAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/saving-accounts")
public class SavingAccountController {
    @Autowired
    private SavingAccountService savingAccountService;

    @GetMapping
    public List<SavingAccount> getAll() {
        return savingAccountService.findAll();
    }
}
