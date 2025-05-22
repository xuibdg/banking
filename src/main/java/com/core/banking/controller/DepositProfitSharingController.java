package com.core.banking.controller;

import com.core.banking.entity.DepositProfitSharing;
import com.core.banking.service.DepositProfitSharingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposit-profit-sharings")
public class DepositProfitSharingController {
    @Autowired
    private DepositProfitSharingService depositProfitSharingService;

    @GetMapping
    public List<DepositProfitSharing> getAll() {
        return depositProfitSharingService.findAll();
    }
}
