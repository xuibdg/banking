package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.SavingAccountDetail.AccountStatementRequestDTO;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.PaginatedResponseDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;
import com.core.banking.service.SavingAccountDetailService;
import com.core.banking.utils.exception.BaseResponse;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/saving-account-details")
@RequiredArgsConstructor
@Validated
public class SavingAccountDetailController extends BaseCRUDController {

    private final SavingAccountDetailService savingAccountDetailService;

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<SavingTransactionResponseDTO> recordDeposit(
            @Valid @RequestBody DepositRequestDTO request,
            @CurrentUser UserMetaData userMetaData) {
        SavingTransactionResponseDTO responseData = savingAccountDetailService.recordDeposit(request);
        return buildCreatedResponse(responseData);
    }

    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<SavingTransactionResponseDTO> recordWithdrawal(
            @Valid @RequestBody WithdrawalRequestDTO request,
            @CurrentUser UserMetaData userMetaData) {
        SavingTransactionResponseDTO responseData = savingAccountDetailService.recordWithdrawal(request);
        return buildCreatedResponse(responseData);
    }

    @GetMapping("/statement")
    public PaginatedResponseDTO<SavingTransactionResponseDTO> getAccountStatement(
            @RequestParam(name = "savingAccountNumber")
            @NotBlank(message = "Saving account number is required.") String savingAccountNumber,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateInput,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateInput,
            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater.") int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1.") @Max(value = 100, message = "Page size must not exceed 100.") int size,
            @CurrentUser UserMetaData currentUser) {

        Timestamp startTimestamp = null;
        if (startDateInput != null) {
            startTimestamp = Timestamp.valueOf(startDateInput.atStartOfDay());
        }

        Timestamp endTimestamp = null;
        if (endDateInput != null) {
            endTimestamp = Timestamp.valueOf(endDateInput.atTime(LocalTime.MAX));
        }

        AccountStatementRequestDTO requestDTO = AccountStatementRequestDTO.builder()
                .savingAccountNumber(savingAccountNumber)
                .startDate(startTimestamp)
                .endDate(endTimestamp)
                .page(page)
                .size(size)
                .build();

        PaginatedResponseDTO<SavingTransactionResponseDTO> responseData = savingAccountDetailService.getAccountStatement(requestDTO);
        return responseData;
    }
}