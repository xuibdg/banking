package com.core.banking.controller;

import com.core.banking.dto.LoanTypeRequest;
import com.core.banking.entity.LoanType;
import com.core.banking.service.LoanTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-types")
public class LoanTypeController {
    @Autowired
    private LoanTypeService loanTypeService;

    @GetMapping
    public List<LoanType> getAll() {
        return loanTypeService.findAll();
    }

    @PostMapping("/create")
    public String createLoanType (@RequestBody LoanTypeRequest request) {
        return loanTypeService.createLoanType(request);
    }

    @PutMapping("/update")
    public String updateLoanType (@PathVariable String loanTypeId, @RequestBody LoanTypeRequest request) {
        return loanTypeService.updateLoanType(loanTypeId, request);
    }

    @DeleteMapping("/delete")
    public String deleteLoanType (@PathVariable String loanTypeId) {
        return loanTypeService.deleteLoanType(loanTypeId);
    }
}
