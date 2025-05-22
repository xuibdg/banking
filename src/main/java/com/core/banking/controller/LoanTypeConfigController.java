package com.core.banking.controller;

import com.core.banking.entity.LoanTypeConfig;
import com.core.banking.service.LoanTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/loan-type-configs")
public class LoanTypeConfigController {
    @Autowired
    private LoanTypeConfigService loanTypeConfigService;

    @GetMapping
    public List<LoanTypeConfig> getAll() {
        return loanTypeConfigService.findAll();
    }
}
