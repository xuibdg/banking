package com.core.banking.controller;

import com.core.banking.entity.DepositoProfitSharing;
import com.core.banking.service.DepositoProfitSharingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposito-profit-sharings")
public class DepositoProfitSharingController {
    @Autowired
    private DepositoProfitSharingService depositoProfitSharingService;

    @GetMapping
    public List<DepositoProfitSharing> getAll() {
        return depositoProfitSharingService.findAll();
    }
}
