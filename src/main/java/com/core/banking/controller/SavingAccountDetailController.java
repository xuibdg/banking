package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.InterBankTransferRequestDTO;
import com.core.banking.dto.SavingAccountDetail.PaginatedResponseDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.SavingAccountDetailService;
import com.core.banking.utils.exception.BaseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/saving-account-details")
@RequiredArgsConstructor
@Validated
public class SavingAccountDetailController {

    private final SavingAccountDetailService savingAccountDetailService;

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<SavingTransactionResponseDTO> recordDeposit(
            @Valid @RequestBody DepositRequestDTO request,
            @CurrentUser UserMetaData userMetaData) {
        SavingTransactionResponseDTO responseData = savingAccountDetailService.recordDeposit(request, userMetaData);
        return BaseCRUDController.buildSuccessResponse(responseData);
    }

    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<SavingTransactionResponseDTO> recordWithdrawal(
            @Valid @RequestBody WithdrawalRequestDTO request,
            @CurrentUser UserMetaData userMetaData) {
        SavingTransactionResponseDTO responseData = savingAccountDetailService.recordWithdrawal(request, userMetaData);
        return BaseCRUDController.buildSuccessResponse(responseData);
    }

    @PostMapping("/transfer-internal")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<SavingTransactionResponseDTO> performInternalTransfer(
            @Valid @RequestBody InterBankTransferRequestDTO request,
            @CurrentUser UserMetaData userMetaData) {
        SavingTransactionResponseDTO responseData = savingAccountDetailService.performInternalTransfer(request, userMetaData);
        return BaseCRUDController.buildSuccessResponse(responseData);
    }


    @GetMapping("/statement")
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResponseDTO<SavingTransactionResponseDTO> getAccountStatement(
            @RequestParam(name = "savingAccountNumber")
            @NotBlank(message = "Saving account number is required.") String savingAccountNumber,

            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(name = "page", defaultValue = "0")
            @Min(value = 0, message = "Page number must be 0 or greater.") int page,

            @RequestParam(name = "size", defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1.")
            @Max(value = 100, message = "Page size must not exceed 100.") int size
    ) {
        return savingAccountDetailService.getAccountStatement(
                savingAccountNumber,
                startDate,
                endDate,
                page,
                size
        );
    }
}