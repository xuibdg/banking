package com.core.banking.service;

import com.core.banking.dto.SavingAccountDetail.AccountStatementRequestDTO;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.PaginatedResponseDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;

public interface SavingAccountDetailService {

    SavingTransactionResponseDTO recordDeposit(DepositRequestDTO depositRequestDTO);

    SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO withdrawalRequestDTO);

    PaginatedResponseDTO<SavingTransactionResponseDTO> getAccountStatement(
            AccountStatementRequestDTO statementRequestDTO
    );
}