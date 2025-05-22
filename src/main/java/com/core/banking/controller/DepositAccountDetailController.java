package com.core.banking.controller;

import com.core.banking.entity.DepositAccountDetail;
import com.core.banking.service.DepositAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposit-account-details")
public class DepositAccountDetailController {
    @Autowired
    private DepositAccountDetailService depositAccountDetailService;

    @GetMapping
    public List<DepositAccountDetail> getAll() {
        return depositAccountDetailService.findAll();
    }
}
