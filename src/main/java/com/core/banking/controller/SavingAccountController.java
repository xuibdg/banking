package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.SavingAccountRequest;
import com.core.banking.dto.SavingAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.enums.SavingAccountStatus;
import com.core.banking.service.MUserService;
import com.core.banking.service.SavingAccountService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/saving-account")
public class SavingAccountController {

    @Autowired
    private SavingAccountService savingAccountService;

    @Autowired
    private MUserService mUserService;

    @PostMapping("/create")
    BaseResponse <SavingAccountResponse> create(@RequestBody @Validated SavingAccountRequest request,
                                                                @CurrentUser UserMetaData userMetaData){
        return buildSuccessResponse(savingAccountService.create(request, userMetaData));
    }

    @GetMapping("/all")
    BaseResponse <List<SavingAccountResponse>> getAll() {
        return buildSuccessResponse(savingAccountService.getAll());
    }

    @GetMapping("/number/{accountNumber}")
    BaseResponse <SavingAccountResponse> getByAccountNumber(@PathVariable String accountNumber) {
        return buildSuccessResponse(savingAccountService.getByAccountNumber(accountNumber));
    }

    @PutMapping("/status/{id}")
    BaseResponse <SavingAccountResponse> updateStatus(
            @PathVariable @Validated String id,
            @RequestParam SavingAccountStatus status, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(savingAccountService.updateStatus(id, status, userMetaData));
    }

    @DeleteMapping("/{accountNumber}")
    BaseResponse <String> deleteAccount(@PathVariable @Validated String accountNumber,
                                                        @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(savingAccountService.deleted(accountNumber, userMetaData));
    }
}
