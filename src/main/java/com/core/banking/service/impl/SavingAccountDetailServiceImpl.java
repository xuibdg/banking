package com.core.banking.service.impl;

import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.InterBankTransferRequestDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingAccountDetail;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.SavingAccountStatus;
import com.core.banking.enums.SavingTransactionType;
import com.core.banking.enums.TransactionTypeStatus;
import com.core.banking.repository.SavingAccountDetailRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.EscrowAccountDetailService;
import com.core.banking.service.SavingAccountDetailService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SavingAccountDetailServiceImpl implements SavingAccountDetailService {

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private SavingAccountDetailRepository savingAccountDetailRepository;

    @Autowired
    private EscrowAccountDetailService escrowAccountDetailService;

    private static final String DESC_INITIAL_DEPOSIT_TELLER = "Initial Deposit Melalui Teller";
    private static final String DESC_DEPOSIT_DEFAULT = "Deposit";
    private static final String DESC_OPENING_FEE = "Biaya Pembukaan Rekening";
    private static final String CHANNEL_SYSTEM = "SYSTEM";
    private static final String PREFIX_FEE = "FEE-";
    private static final String PREFIX_DEPOSIT = "DEP";
    private static final String PREFIX_WITHDRAWAL = "WDR";
    private static final String PREFIX_INITIAL = "INIT";

    @Override
    @Transactional
    public SavingTransactionResponseDTO recordDeposit(DepositRequestDTO request, UserMetaData userMetaData) {
        validateDepositRequest(request);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        SavingAccount savingAccount = findAndLockSavingAccount(request.getSavingAccountNumber());
        SavingTypeConfig config = getActiveSavingConfig(savingAccount);

        String escrowRef = processOneStepEscrowCreationAndRelease(
                savingAccount,
                request.getPayerCustomerId(),
                request.getAmount(),
                userMetaData
        );

        boolean isInitialDeposit = !savingAccountDetailRepository.existsBySavingAccount(savingAccount);
        SavingTransactionResponseDTO response;

        if (isInitialDeposit) {
            validateInitialDepositState(savingAccount);
            validateInitialDepositRules(request.getAmount(), config);

            String savingRef = generateSavingTransactionReference(PREFIX_INITIAL, savingAccount, SavingTransactionType.INITIAL_DEPOSIT, escrowRef);
            SavingAccountDetail initialDepositDetail = createAndSaveSavingDetail(savingAccount, SavingTransactionType.INITIAL_DEPOSIT, MutationType.CREDIT, request.getAmount(), DESC_INITIAL_DEPOSIT_TELLER, savingRef, request.getChannel(), now);
            updateSavingAccountBalance(savingAccount, initialDepositDetail.getEndBalance(), now);
            processOpeningFee(savingAccount, config, savingRef, now);
            activateAccount(savingAccount, now);
            response = mapToTransactionResponseDTO(initialDepositDetail);
        } else {
            validateAccountIsActive(savingAccount);
            BigDecimal endBalanceSaving = savingAccount.getCurrentBalance().add(request.getAmount());
            validateTransactionRules(savingAccount, config, request.getAmount(), MutationType.CREDIT, endBalanceSaving);

            String savingRef = generateSavingTransactionReference(PREFIX_DEPOSIT, savingAccount, SavingTransactionType.DEPOSIT, escrowRef);
            String description = (request.getDescription() != null && !request.getDescription().isBlank()) ? request.getDescription() : DESC_DEPOSIT_DEFAULT;
            SavingAccountDetail savingDetail = createAndSaveSavingDetail(savingAccount, SavingTransactionType.DEPOSIT, MutationType.CREDIT, request.getAmount(), description, savingRef, request.getChannel(), now);
            updateSavingAccountBalance(savingAccount, savingDetail.getEndBalance(), now);
            response = mapToTransactionResponseDTO(savingDetail);
        }
        return response;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO request, UserMetaData userMetaData) {
        validateWithdrawalRequest(request);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        SavingAccount savingAccount = findAndLockSavingAccount(request.getSavingAccountNumber());
        SavingTypeConfig config = getActiveSavingConfig(savingAccount);
        validateAccountIsActive(savingAccount);
        if (savingAccount.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.INSUFFICIENT_BALANCE);
        }
        BigDecimal endBalanceSaving = savingAccount.getCurrentBalance().subtract(request.getAmount());
        validateTransactionRules(savingAccount, config, request.getAmount(), MutationType.DEBIT, endBalanceSaving);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String savingRef = generateSavingTransactionReference(PREFIX_WITHDRAWAL, savingAccount, SavingTransactionType.WITHDRAWAL, uniqueSuffix);
        SavingAccountDetail savingDetail = createAndSaveSavingDetail(savingAccount, SavingTransactionType.WITHDRAWAL, MutationType.DEBIT, request.getAmount(), request.getDescription(), savingRef, request.getChannel(), now);
        updateSavingAccountBalance(savingAccount, savingDetail.getEndBalance(), now);
        return mapToTransactionResponseDTO(savingDetail);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SavingTransactionResponseDTO performInternalTransfer(InterBankTransferRequestDTO request, UserMetaData userMetaData) {
        validateInternalTransferRequest(request);

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        String internalTransferReference = "TRF-" + UUID.randomUUID().toString();

        SavingAccount sourceAccount = findAndLockSavingAccount(request.getSourceAccountNumber());
        validateAccountIsActive(sourceAccount);
        SavingTypeConfig sourceConfig = getActiveSavingConfig(sourceAccount);

        SavingAccount destinationAccount = findAndLockSavingAccount(request.getDestinationAccountNumber());
        validateAccountIsActive(destinationAccount);
        SavingTypeConfig destinationConfig = getActiveSavingConfig(destinationAccount);

        if (sourceAccount.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.INSUFFICIENT_BALANCE);
        }

        BigDecimal finalSourceBalance = sourceAccount.getCurrentBalance().subtract(request.getAmount());
        validateTransactionRules(sourceAccount, sourceConfig, request.getAmount(), MutationType.DEBIT, finalSourceBalance);

        BigDecimal finalDestinationBalance = destinationAccount.getCurrentBalance().add(request.getAmount());
        validateTransactionRules(destinationAccount, destinationConfig, request.getAmount(), MutationType.CREDIT, finalDestinationBalance);

        String debitDescription = String.format("Transfer ke %s - %s",
                destinationAccount.getAccountNumber(),
                request.getDescription() != null ? request.getDescription() : "");

        SavingAccountDetail debitDetail = createAndSaveSavingDetail(
                sourceAccount,
                SavingTransactionType.TRANSFER_INTERNAL_DEBIT,
                MutationType.DEBIT,
                request.getAmount(),
                debitDescription,
                internalTransferReference,
                request.getChannel(),
                now
        );
        updateSavingAccountBalance(sourceAccount, debitDetail.getEndBalance(), now);

        String creditDescription = String.format("Transfer dari %s - %s",
                sourceAccount.getAccountNumber(),
                request.getDescription() != null ? request.getDescription() : "");

        SavingAccountDetail creditDetail = createAndSaveSavingDetail(
                destinationAccount,
                SavingTransactionType.TRANSFER_INTERNAL_CREDIT,
                MutationType.CREDIT,
                request.getAmount(),
                creditDescription,
                internalTransferReference,
                request.getChannel(),
                now
        );
        updateSavingAccountBalance(destinationAccount, creditDetail.getEndBalance(), now);

        return mapToTransactionResponseDTO(debitDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SavingTransactionResponseDTO> getAccountStatement(String savingAccountNumber, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        validateAccountStatementParams(savingAccountNumber, pageable.getPageNumber(), pageable.getPageSize());
        Timestamp startTimestamp = (startDate != null) ? Timestamp.valueOf(startDate.atStartOfDay()) : null;
        Timestamp endTimestamp = (endDate != null) ? Timestamp.valueOf(endDate.atTime(LocalTime.MAX)) : null;
        if (startTimestamp != null && endTimestamp != null && endTimestamp.before(startTimestamp)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_DATE_RANGE);
        }
        SavingAccount savingAccount = savingAccountRepository.findByAccountNumber(savingAccountNumber)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
        Page<SavingTransactionResponseDTO> pageResult = savingAccountDetailRepository.findBySavingAccountAndDateRange(savingAccount.getSavingAccountId(), startTimestamp, endTimestamp, pageable)
                .map(data -> {
                    SavingTransactionResponseDTO dto = mapToTransactionResponseDTO(data);
                    dto.setSavingAccountNumber(savingAccount.getAccountNumber());
                    return dto;
                });
        return pageResult;
    }

    private SavingAccount findAndLockSavingAccount(String accountNumber) {
        return savingAccountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
    }

    private String processOneStepEscrowCreationAndRelease(SavingAccount beneficiaryAccount, String payerCustomerId, BigDecimal amount, UserMetaData user) {
        EscrowAccountRequest escrowRequest = EscrowAccountRequest.builder()
                .payerCustomer(payerCustomerId)
                .beneficiaryCustomer(String.valueOf(beneficiaryAccount.getCustomer().getId()))
                .purpose("Deposit to Savings Account: " + beneficiaryAccount.getAccountNumber())
                .transactionTypeStatus(TransactionTypeStatus.SAVING_PAYMENT)
                .savingAccount(beneficiaryAccount.getSavingAccountId())
                .build();

        String description = "Auto-generated deposit to " + beneficiaryAccount.getAccountNumber();

        return escrowAccountDetailService.createAndReleaseEscrowAccount(
                escrowRequest,
                amount,
                beneficiaryAccount.getAccountNumber(),
                description,
                user
        );
    }

    private String generateSavingTransactionReference(String prefix, SavingAccount account, SavingTransactionType type, String externalRef) {
        String cleanExternalRef = externalRef;
        if (externalRef != null && externalRef.contains("-")) {
            String[] parts = externalRef.split("-");
            cleanExternalRef = parts[parts.length - 2] + parts[parts.length - 1];
        }

        if (type == SavingTransactionType.INITIAL_DEPOSIT) {
            return String.format("%s-1-%s", prefix, cleanExternalRef);
        }
        long count = savingAccountDetailRepository.countBySavingAccountAndTransactionType(account, type);
        return String.format("%s-%d-%s", prefix, count + 1, cleanExternalRef);
    }

    private SavingAccountDetail createAndSaveSavingDetail(SavingAccount account, SavingTransactionType trxType, MutationType mutation, BigDecimal amount, String desc, String ref, String channel, Timestamp trxAt) {
        BigDecimal beginBalance = account.getCurrentBalance();
        BigDecimal endBalance = (mutation == MutationType.CREDIT) ? beginBalance.add(amount) : beginBalance.subtract(amount);
        SavingAccountDetail detail = SavingAccountDetail.builder()
                .savingAccount(account)
                .transactionType(trxType)
                .mutationType(mutation)
                .nominalTransaction(amount)
                .beginBalance(beginBalance)
                .endBalance(endBalance)
                .description(desc)
                .transactionReference(ref)
                .channel(channel != null ? channel : CHANNEL_SYSTEM)
                .transactionAt(trxAt)
                .createdAt(trxAt)
                .build();
        return savingAccountDetailRepository.save(detail);
    }

    private void updateSavingAccountBalance(SavingAccount account, BigDecimal newBalance, Timestamp now) {
        account.setCurrentBalance(newBalance);
        account.setLastTransactionAt(now);
        account.setUpdatedAt(now);
        savingAccountRepository.save(account);
    }

    private void activateAccount(SavingAccount account, Timestamp now) {
        if (account.getAccountStatus() != SavingAccountStatus.ACTIVE) {
            account.setAccountStatus(SavingAccountStatus.ACTIVE);
            account.setUpdatedAt(now);
            savingAccountRepository.save(account);
        }
    }

    private void processOpeningFee(SavingAccount account, SavingTypeConfig config, String relatedReference, Timestamp now) {
        BigDecimal openingFee = Optional.ofNullable(config.getMonthlyMaintenanceFee()).orElse(BigDecimal.ZERO);
        if (openingFee.compareTo(BigDecimal.ZERO) > 0) {
            String feeTransactionReference = PREFIX_FEE + relatedReference;
            SavingAccountDetail feeDetail = createAndSaveSavingDetail(account, SavingTransactionType.FEE_DEBIT, MutationType.DEBIT, openingFee, DESC_OPENING_FEE, feeTransactionReference, CHANNEL_SYSTEM, now);
            updateSavingAccountBalance(account, feeDetail.getEndBalance(), now);
        }
    }

    private SavingTransactionResponseDTO mapToTransactionResponseDTO(SavingAccountDetail detail) {
        return SavingTransactionResponseDTO.builder()
                .transactionId(detail.getSavingAccountDetailId())
                .savingAccountNumber(detail.getSavingAccount().getAccountNumber())
                .transactionType(detail.getTransactionType())
                .mutationType(detail.getMutationType())
                .amount(detail.getNominalTransaction())
                .balanceBefore(detail.getBeginBalance())
                .balanceAfter(detail.getEndBalance())
                .description(detail.getDescription())
                .transactionReference(detail.getTransactionReference())
                .channel(detail.getChannel())
                .transactionTimestamp(detail.getTransactionAt())
                .createdAt(detail.getCreatedAt())
                .build();
    }

    // --- Validation Helper Methods ---

    private void validateAccountIsActive(SavingAccount account) {
        if (account.getAccountStatus() != SavingAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ACCOUNT_NOT_ACTIVE);
        }
    }

    private SavingTypeConfig getActiveSavingConfig(SavingAccount account) {
        SavingTypeConfig config = account.getSavingTypeConfig();
        if (config == null || !config.getIsActive()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalErrorMapping.SAVING_CONFIG_NOT_FOUND);
        }
        return config;
    }

    private void validateInitialDepositState(SavingAccount account) {
        if (account.getAccountStatus() == SavingAccountStatus.CLOSED || account.getAccountStatus() == SavingAccountStatus.DORMANT) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ACCOUNT_NOT_ACTIVE);
        }
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.SAVING_ACCOUNT_HAS_TRANSACTION);
        }
    }

    private void validateTransactionRules(SavingAccount account, SavingTypeConfig config, BigDecimal amount, MutationType mutationType, BigDecimal endBalance) {
        if (mutationType == MutationType.CREDIT && config.getMaxBalanceLimit() != null && endBalance.compareTo(config.getMaxBalanceLimit()) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.MAX_BALANCE_EXCEEDED);
        }
        if (mutationType == MutationType.DEBIT && config.getMinBalanceLimit() != null && endBalance.compareTo(config.getMinBalanceLimit()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.MIN_BALANCE_VIOLATED);
        }
        validateDailyTransactionLimit(account, amount, mutationType, config.getDailyTransactionLimit());
        validateDailyTransactionCount(account, config.getDailyTransactionCountLimit());
    }

    private void validateInitialDepositRules(BigDecimal amount, SavingTypeConfig config) {
        if (config.getMinInitialDeposit() != null && amount.compareTo(config.getMinInitialDeposit()) < 0) {
            String errorMessage = formatErrorMessage(GlobalErrorMapping.MINIMUM_INITIAL_DEPOSIT, config.getMinInitialDeposit().toPlainString());
            throw new BusinessException(HttpStatus.BAD_REQUEST, errorMessage, GlobalErrorMapping.MINIMUM_INITIAL_DEPOSIT.code);
        }
    }

    private void validateDailyTransactionCount(SavingAccount account, Integer dailyCountLimit) {
        if (dailyCountLimit == null || dailyCountLimit <= 0) return;
        long todayTransactionCount = countTransactionsToday(account);
        if (todayTransactionCount >= dailyCountLimit) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.DAILY_COUNT_LIMIT_EXCEEDED);
        }
    }

    private void validateDailyTransactionLimit(SavingAccount account, BigDecimal amount, MutationType type, BigDecimal limit) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal sumToday = sumTransactionsToday(account, type);
        if (sumToday.add(amount).compareTo(limit) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.DAILY_NOMINAL_LIMIT_EXCEEDED);
        }
    }

    private long countTransactionsToday(SavingAccount account) {
        LocalDate today = LocalDate.now();
        return savingAccountDetailRepository.countBySavingAccountAndTransactionAtBetween(account, Timestamp.valueOf(today.atStartOfDay()), Timestamp.valueOf(today.atTime(LocalTime.MAX)));
    }

    private BigDecimal sumTransactionsToday(SavingAccount account, MutationType type) {
        LocalDate today = LocalDate.now();
        return Optional.ofNullable(savingAccountDetailRepository.sumTransactionsByAccountAndMutationTypeAndDate(account, type, Timestamp.valueOf(today.atStartOfDay()), Timestamp.valueOf(today.atTime(LocalTime.MAX)))).orElse(BigDecimal.ZERO);
    }

    private String formatErrorMessage(GlobalErrorMapping mapping, String... params) {
        String message = mapping.message;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replace("${" + (i + 1) + "}", params[i]);
            }
        }
        return message;
    }

    private void validateDepositRequest(DepositRequestDTO request) {
        if (request.getSavingAccountNumber() == null || request.getSavingAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_DEPOSIT_AMOUNT);
        }
        if (request.getPayerCustomerId() == null || request.getPayerCustomerId().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.PAYER_CUSTOMER_NOT_FOUND);
        }
    }

    private void validateWithdrawalRequest(WithdrawalRequestDTO request) {
        if (request.getSavingAccountNumber() == null || request.getSavingAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_WITHDRAWAL_AMOUNT);
        }
    }

    private void validateAccountStatementParams(String savingAccountNumber, int page, int size) {
        if (savingAccountNumber == null || savingAccountNumber.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.MISSING_ACCOUNT_ID);
        }
        if (page < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_PAGE_PARAM);
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_PAGE_SIZE_PARAM);
        }
    }

    private void validateInternalTransferRequest(InterBankTransferRequestDTO request) {
        if (request.getSourceAccountNumber() == null || request.getSourceAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
        }
        if (request.getDestinationAccountNumber() == null || request.getDestinationAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
        }
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SOURCE_AND_DESTINATION_CANT_BE_THE_SAME);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_NOMINAL_INVALID);
        }
    }
}