package com.core.banking.service;

import com.core.banking.dto.SavingAccountDetail.AccountStatementRequestDTO;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.PaginatedResponseDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;

import java.time.LocalDate;

public interface SavingAccountDetailService {

    SavingTransactionResponseDTO recordDeposit(DepositRequestDTO depositRequestDTO);

    SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO withdrawalRequestDTO);

    PaginatedResponseDTO<SavingTransactionResponseDTO> getAccountStatement(
            String savingAccountNumber,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size);
}

