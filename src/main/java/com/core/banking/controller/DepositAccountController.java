package com.core.banking.controller;

import com.core.banking.entity.DepositoAccount;
import com.core.banking.service.DepositoAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposito-accounts")
public class DepositoAccountController {
    @Autowired
    private DepositoAccountService depositoAccountService;

    @GetMapping
    public List<DepositoAccount> getAll() {
        return depositoAccountService.findAll();
    }
}
