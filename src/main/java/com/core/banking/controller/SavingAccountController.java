package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.SavingAccountRequest;
import com.core.banking.dto.SavingAccountResponse;
import com.core.banking.dto.UpdateSavingAccountStatusRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.SavingAccount;
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
    BaseResponse <ResponseEntity<SavingAccountResponse>> create(@RequestBody @Validated SavingAccountRequest request,
                                                                @CurrentUser UserMetaData userMetaData){
        return buildSuccessResponse(ResponseEntity.ok(savingAccountService.create(request, userMetaData)));
    }

    @GetMapping("/all")
    public List<SavingAccountResponse> getAll() {
        return savingAccountService.getAll();
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<SavingAccountResponse> getByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(savingAccountService.getByAccountNumber(accountNumber));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<SavingAccountResponse> updateStatus(
            @PathVariable String id,
            @RequestParam SavingAccountStatus status) {
        return ResponseEntity.ok(savingAccountService.updateStatus(id, status));
    }
}
