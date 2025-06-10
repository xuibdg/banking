package com.core.banking.service;

import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.InitialDepositRequestDTO; // Jika DTO baru dibuat
import com.core.banking.dto.SavingAccountDetail.PaginatedResponseDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;
import com.core.banking.dto.UserMetaData;
import java.time.LocalDate;

public interface SavingAccountDetailService {

    SavingTransactionResponseDTO recordDeposit(DepositRequestDTO request, UserMetaData userMetaData);

    SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO request, UserMetaData userMetaData);

    SavingTransactionResponseDTO performInitialDeposit(InitialDepositRequestDTO request, UserMetaData userMetaData);

    PaginatedResponseDTO<SavingTransactionResponseDTO> getAccountStatement(
            String savingAccountNumber,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );
}
