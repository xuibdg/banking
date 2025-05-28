package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.dto.LoanTransactionResponse;
import com.core.banking.service.LoanTransactionService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;


import java.util.List;

@RestController
@RequestMapping("/loan-transactions")
public class LoanTransactionController {
    @Autowired
    private LoanTransactionService loanTransactionService;

    @GetMapping
    public List<LoanTransactionResponse> getAll() {
        return loanTransactionService.findAll();
    }

    @PostMapping("/create")
    BaseResponse<String> createLoanTransaction (@RequestBody LoanTransactionRequest request, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTransactionService.createLoanTransaction(request, userMetaData));
    }

    @PostMapping("/{loanAccountId}")
    BaseResponse<LoanTransactionResponse> approveAndDisburseLoan(@PathVariable String loanAccountId, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTransactionService.approveAndDIsburseLoan(loanAccountId, userMetaData));
    }

    @PutMapping("/{loanTransactionId}")
    BaseResponse<String> updateLoanTransaction (@PathVariable String loanTransactionId, @RequestBody LoanTransactionRequest request, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTransactionService.updateLoanTransaction(loanTransactionId,request, userMetaData));
    }

    @DeleteMapping("/{loanTransactionId}")
    BaseResponse<String> deleteLoanTransaction (@PathVariable String loanTransactionId, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTransactionService.deleteLoanTransaction(loanTransactionId, userMetaData));
    }
}
