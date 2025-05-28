package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanAccountRequest;
import com.core.banking.dto.LoanAccountResponse;
import com.core.banking.service.LoanAccountService;
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
@RequestMapping("/loan-accounts")
public class LoanAccountController {
    @Autowired
    private LoanAccountService loanAccountService;

    @GetMapping
    public List<LoanAccountResponse> getAll() {
        return loanAccountService.findAll();
    }

    @PostMapping("/create")
    BaseResponse<String> createLoanAccount(@RequestBody LoanAccountRequest request,
                                           @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanAccountService.createLoanAccount(request, userMetaData));
    }

    @PutMapping("/{loanAccountId}")
    BaseResponse<String> updateLoanAccount (@PathVariable String loanAccountId,@RequestBody LoanAccountRequest request,
                                            @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanAccountService.updateLoanAccount(loanAccountId,request, userMetaData));
    }

    @DeleteMapping("/{loanAccountId}")
    BaseResponse<String> deleteLoanAccount (@PathVariable String loanAccountId,
                                            @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanAccountService.deleteLoanAccount(loanAccountId, userMetaData));
    }
}
