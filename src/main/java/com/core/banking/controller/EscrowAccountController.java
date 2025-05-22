package com.core.banking.controller;

import com.core.banking.entity.EscrowAccount;
import com.core.banking.service.EscrowAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/escrow-accounts")
public class EscrowAccountController {
    @Autowired
    private EscrowAccountService escrowAccountService;

    @GetMapping
    public List<EscrowAccount> getAll() {
        return escrowAccountService.findAll();
    }
}
