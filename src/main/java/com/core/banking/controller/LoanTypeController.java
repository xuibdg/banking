package com.core.banking.controller;

import com.core.banking.entity.LoanType;
import com.core.banking.service.LoanTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/loan-types")
public class LoanTypeController {
    @Autowired
    private LoanTypeService loanTypeService;

    @GetMapping
    public List<LoanType> getAll() {
        return loanTypeService.findAll();
    }
}
