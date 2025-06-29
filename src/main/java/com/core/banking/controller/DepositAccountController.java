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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/deposit-accounts")
public class DepositAccountController {
    @Autowired
    private DepositAccountService depositAccountService;

    @GetMapping
    public BaseResponse<List<DepositAccount>> getAllDepositAccounts() {
        return buildSuccessResponse(depositAccountService.findAll());
    }

    @PostMapping
    BaseResponse<DepositAccountResponse> createDepositAccount(@RequestBody DepositAccountRequest depositAccountRequest, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(depositAccountService.createDepositAccount(depositAccountRequest, userMetaData));
    }

    @GetMapping("/{id}")
    BaseResponse<DepositAccountResponse> getDepositAccountById(@PathVariable("id") Long id) {
        return buildSuccessResponse(depositAccountService.getDepositAccountById(id));
    }

    @GetMapping("/customers/{customerId}")
    BaseResponse<List<DepositAccountResponse>> getDepositAccountByCustomerId(@PathVariable("customerId") String customerId) {
        return buildSuccessResponse(depositAccountService.getDepositAccountsByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    BaseResponse<List<DepositAccountResponse>> getDepositAccountsByStatus(@PathVariable("status") DepositAccountStatus status) {
        return buildSuccessResponse(depositAccountService.getDepositAccountsByStatus(status));
    }

    @PostMapping("/{depositAccountId}/bilyet")
    public BaseResponse<Map<String, Object>> generateBilyet(@PathVariable("depositAccountId") Long depositAccountId, @CurrentUser UserMetaData userMetaData) {
        Map<String, Object> bilyetData = depositAccountService.generateBilyet(depositAccountId, userMetaData);
        return buildSuccessResponse(bilyetData);
    }

//    @DeleteMapping("/{id}")
//    BaseResponse<String> deleteDepositAccount(@PathVariable("id") Long id){
//        return buildSuccessResponse(depositAccountService.deleteDepositAccount(id));
//    }
}