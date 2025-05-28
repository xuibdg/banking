package com.core.banking.controller;

import com.core.banking.dto.SavingAccountDetail.AccountStatementRequestDTO;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.PaginatedResponseDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;
import com.core.banking.service.SavingAccountDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/saving-accounts/details")
public class SavingAccountDetailController {

    private static final Logger logger = LoggerFactory.getLogger(SavingAccountDetailController.class);

    private final SavingAccountDetailService savingAccountDetailService;

    @Autowired
    public SavingAccountDetailController(SavingAccountDetailService savingAccountDetailService) {
        this.savingAccountDetailService = savingAccountDetailService;
    }


    @PostMapping("/deposit")
    public ResponseEntity<SavingTransactionResponseDTO> recordDeposit(

            @RequestBody DepositRequestDTO depositRequestDTO) {
        SavingTransactionResponseDTO response = savingAccountDetailService.recordDeposit(depositRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/withdrawal")
    public ResponseEntity<SavingTransactionResponseDTO> recordWithdrawal(
            @RequestBody WithdrawalRequestDTO withdrawalRequestDTO) {
        SavingTransactionResponseDTO response = savingAccountDetailService.recordWithdrawal(withdrawalRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/statement")
    public ResponseEntity<PaginatedResponseDTO<SavingTransactionResponseDTO>> getAccountStatement(
            AccountStatementRequestDTO statementRequestDTO) {
        PaginatedResponseDTO<SavingTransactionResponseDTO> response = savingAccountDetailService.getAccountStatement(statementRequestDTO);
        return ResponseEntity.ok(response);
    }
}
