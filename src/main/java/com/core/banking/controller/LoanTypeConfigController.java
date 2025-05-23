package com.core.banking.controller;

import com.core.banking.dto.LoanTypeConfigRequest;
import com.core.banking.entity.LoanTypeConfig;
import com.core.banking.service.LoanTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-type-configs")
public class LoanTypeConfigController {
    @Autowired
    private LoanTypeConfigService loanTypeConfigService;

    @GetMapping
    public List<LoanTypeConfig> getAll() {
        return loanTypeConfigService.findAll();
    }

    @PostMapping("/create")
    public String createLoanTypeConfig (@RequestBody LoanTypeConfigRequest request) {
        return loanTypeConfigService.createLoanTypeConfig(request);
    }

    @PutMapping("/{loanTypeConfigId}")
    public String updateLoanTypeConfig (@PathVariable String loanTypeConfigId, @RequestBody LoanTypeConfigRequest request) {
        return loanTypeConfigService.updateLoanTypeConfig(loanTypeConfigId, request);
    }

    @DeleteMapping("/{loanTypeConfigId}")
    public String deleteLoanTypeConfig (@PathVariable String loanTypeConfigId) {
        return loanTypeConfigService.deleteLoanTypeConfig(loanTypeConfigId);
    }
}
