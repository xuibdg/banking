package com.core.banking.service;

import com.core.banking.dto.SavingAccountDetail.*;
import com.core.banking.dto.UserMetaData;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface SavingAccountDetailService {

    SavingTransactionResponseDTO recordDeposit(DepositRequestDTO request, UserMetaData userMetaData);

    SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO request, UserMetaData userMetaData);

    PaginatedResponseDTO<SavingTransactionResponseDTO> getAccountStatement(
            String savingAccountNumber,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );
    SavingTransactionResponseDTO performInternalTransfer(InterBankTransferRequestDTO request, UserMetaData userMetaData);
}
