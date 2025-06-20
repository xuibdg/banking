package com.core.banking.service;

import com.core.banking.dto.SavingAccountDetail.*;
import com.core.banking.dto.UserMetaData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface SavingAccountDetailService {

    SavingTransactionResponseDTO recordDeposit(DepositRequestDTO request, UserMetaData userMetaData);

    SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO request, UserMetaData userMetaData);

    Page<SavingTransactionResponseDTO> getAccountStatement(
            String savingAccountNumber,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
    SavingTransactionResponseDTO performInternalTransfer(InterBankTransferRequestDTO request, UserMetaData userMetaData);
}
