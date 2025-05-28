package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.DepositAccountRequest;
import com.core.banking.dto.DepositAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.DepositAccount;
import com.core.banking.enums.DepositAccountStatus;
import com.core.banking.service.DepositAccountService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/deposit-accounts")
public class DepositAccountController {
    @Autowired
    private DepositAccountService depositAccountService;

    @GetMapping
    public BaseResponse <List<DepositAccount>> getAll() {
        return buildSuccessResponse(depositAccountService.findAll());
    }

    @PostMapping
    BaseResponse<DepositAccountResponse> openDepositAccount(@RequestBody DepositAccountRequest depositAccountRequest, UserMetaData userMetaData) {
        return buildSuccessResponse(depositAccountService.openDepositAccount(depositAccountRequest, userMetaData));
    }

    @GetMapping("/{id}")
    BaseResponse<DepositAccountResponse> getDepositAccountById(@PathVariable("id") Long id) {
        return buildSuccessResponse(depositAccountService.getDepositAccountById(id));
    }

    @GetMapping("/customer/{customerId}")
    BaseResponse<List<DepositAccountResponse>> getDepositAccountsByCustomerId(@PathVariable("customerId") String customerId) {
    return buildSuccessResponse(depositAccountService.getDepositAccountsByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    BaseResponse<List<DepositAccountResponse>> getDepositAccountsByStatus(@PathVariable("status") DepositAccountStatus status) {
        return buildSuccessResponse(depositAccountService.getDepositAccountsByStatus(status));
    }

    @DeleteMapping("/{id}")
    BaseResponse<String> deleteDepositAccount(@PathVariable("id") Long id){
    return buildSuccessResponse(depositAccountService.deleteDepositAccount(id));
    }
}
