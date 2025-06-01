package com.core.banking.service.impl;

import com.core.banking.dto.DepositAccountResponse;
import com.core.banking.dto.EarlyWithdrawalResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.DepositAccount;
import com.core.banking.entity.DepositAccountDetail;
import com.core.banking.enums.DepositAccountStatus;
import com.core.banking.enums.DepositoTransactionType;
import com.core.banking.enums.MutationType;
import com.core.banking.repository.DepositAccountDetailRepository;
import com.core.banking.repository.DepositAccountRepository;
import com.core.banking.service.EarlyWithdrawalService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class EarlyWithdrawalServiceImpl implements EarlyWithdrawalService {
    @Autowired
    DepositAccountRepository depositAccountRepository;

    @Autowired
    DepositAccountDetailRepository depositAccountDetailRepository;

    @Override
    public EarlyWithdrawalResponse processEarlyWithdrawal(Long depositAccountId, UserMetaData userMetaData) {
        DepositAccount depositAccount = depositAccountRepository.findById(depositAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));

        if (depositAccount.getAccountStatus() != DepositAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_ACTIVE);
        }
        BigDecimal penaltyPercentage = depositAccount.getDepositTypeConfig().getEarlyWithdrawalPenaltyPercentage();
        BigDecimal penaltyAmount = depositAccount.getPrincipalAmount().multiply(penaltyPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal amountToReturn = depositAccount.getPrincipalAmount().subtract(penaltyAmount);

        DepositAccountDetail penaltyTransaction = DepositAccountDetail.builder()
                .depositAccount(depositAccount)
                .transactionType(DepositoTransactionType.PENALTY_DEBIT)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(penaltyAmount)
                .beginBalance(depositAccount.getPrincipalAmount())
                .endBalance(depositAccount.getPrincipalAmount().subtract(penaltyAmount))
                .description("Penalti untuk penarikan lebih awal sebelum tanggal jatuh tempo: " + penaltyPercentage + "%")
                .transactionAt(LocalDateTime.now())
                .createdBy(userMetaData.getUserId())
                .build();
        depositAccountDetailRepository.save(penaltyTransaction);

        DepositAccountDetail withdrawalTransaction = DepositAccountDetail.builder()
                .depositAccount(depositAccount)
                .transactionType(DepositoTransactionType.EARLY_WITHDRAWAL)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(amountToReturn)
                .beginBalance(penaltyTransaction.getEndBalance())
                .endBalance(BigDecimal.ZERO)
                .description("Penarikan dana deposito sebelum tanggal jatuh tempo")
                .transactionAt(LocalDateTime.now())
                .createdBy(userMetaData.getUserId())
                .build();
        depositAccountDetailRepository.save(withdrawalTransaction);

        depositAccount.setAccountStatus(DepositAccountStatus.CLOSED_PREMATURELY);
        depositAccount.setClosedAt(LocalDateTime.now());
        depositAccountRepository.save(depositAccount);

        return EarlyWithdrawalResponse.builder()
                .depositAccountId(depositAccount.getDepositoAccountId())
                .accountNumber(depositAccount.getAccountNumber())
                .principalAmount(depositAccount.getPrincipalAmount())
                .penaltyAmount(penaltyAmount)
                .penaltyPercentage(penaltyPercentage)
                .returnedAmount(amountToReturn)
                .withdrawalDate(LocalDate.from(LocalDateTime.now()))
                .build();
    }

    @Override
    public Map<String, Object> calculateEarlyWithdrawalPenalty(Long depositAccountId) {
        DepositAccount depositAccount = depositAccountRepository.findById(depositAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));

        if (depositAccount.getAccountStatus() != DepositAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_ACTIVE);
        }
        BigDecimal penaltyPercentage = depositAccount.getDepositTypeConfig().getEarlyWithdrawalPenaltyPercentage();

        BigDecimal penaltyAmount = depositAccount.getPrincipalAmount().multiply(penaltyPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("depositAccountId", depositAccount.getDepositoAccountId());
        result.put("customerName", depositAccount.getCustomer().getFullName());
        result.put("depositTypeName", depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
        result.put("accountNumber", depositAccount.getAccountNumber());
        result.put("principalAmount", depositAccount.getPrincipalAmount());
        result.put("penaltyPercentage", penaltyPercentage);
        result.put("penaltyAmount", penaltyAmount);
        result.put("estimatedReturnAmount", depositAccount.getPrincipalAmount().subtract(penaltyAmount));

        return result;
    }
}
