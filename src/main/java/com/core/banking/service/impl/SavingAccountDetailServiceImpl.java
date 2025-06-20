package com.core.banking.service.impl;

import com.core.banking.config.AppCoaConfig;
import com.core.banking.dto.JournalDetailRequest;
import com.core.banking.dto.JournalRequest;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.InterBankTransferRequestDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingAccountDetail;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.SavingAccountStatus;
import com.core.banking.enums.SavingTransactionType;
import com.core.banking.repository.MChartOfAccountRepository;
import com.core.banking.repository.SavingAccountDetailRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.JournalLedgerService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SavingAccountDetailServiceImpl implements SavingAccountDetailService {

    @Autowired
    private SavingAccountRepository savingAccountRepository;
    @Autowired
    private SavingAccountDetailRepository savingAccountDetailRepository;
    @Autowired
    private JournalLedgerService journalLedgerService;
    @Autowired
    private MChartOfAccountRepository mChartOfAccountRepository;
    @Autowired
    private AppCoaConfig appCoaConfig;

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

        LocalDate transactionDate = Optional.ofNullable(request.getSystemDate()).orElse(LocalDate.now());
        Timestamp now = Timestamp.valueOf(LocalDateTime.of(transactionDate, LocalTime.now()));

        SavingAccount savingAccount = findAndLockSavingAccount(request.getSavingAccountNumber());
        SavingTypeConfig config = getActiveSavingConfig(savingAccount);

        boolean isInitialDeposit = !savingAccountDetailRepository.existsBySavingAccount(savingAccount);
        SavingAccountDetail lastTransaction;

        if (isInitialDeposit) {
            validateInitialDepositState(savingAccount);
            validateInitialDepositRules(request.getAmount(), config);

            if (request.isJournal()) {
                findActiveCoaByCode(config.getCoaCode());
            }

            String transactionId = UUID.randomUUID().toString();
            String savingRef = generateSavingTransactionReference(PREFIX_INITIAL, savingAccount, SavingTransactionType.INITIAL_DEPOSIT, transactionId);
            lastTransaction = createAndSaveSavingDetail(savingAccount, SavingTransactionType.INITIAL_DEPOSIT, MutationType.CREDIT, request.getAmount(), DESC_INITIAL_DEPOSIT_TELLER, savingRef, request.getChannel(), now);
            updateSavingAccountBalance(savingAccount, lastTransaction.getEndBalance(), now);

            if (request.isJournal()) {
               MChartOfAccount coaProduct = findActiveCoaByCode(config.getCoaCode());
                postJournalEntry(savingRef, "SAVING_INITIAL_DEPOSIT", DESC_INITIAL_DEPOSIT_TELLER, transactionDate, request.getAmount(), appCoaConfig.getCashTellerId(), coaProduct.getId(), userMetaData);
            }

            processOpeningFee(savingAccount, config, lastTransaction.getTransactionReference(), now, transactionDate, request.isJournal(), userMetaData);
            activateAccount(savingAccount, now);

        } else {
            validateAccountIsActive(savingAccount);
            BigDecimal endBalanceSaving = savingAccount.getCurrentBalance().add(request.getAmount());
            validateTransactionRules(savingAccount, config, request.getAmount(), MutationType.CREDIT, endBalanceSaving, transactionDate);

            if (request.isJournal()) {
                findActiveCoaByCode(config.getCoaCode());
            }

            String transactionId = UUID.randomUUID().toString();
            String savingRef = generateSavingTransactionReference(PREFIX_DEPOSIT, savingAccount, SavingTransactionType.DEPOSIT, transactionId);
            String description = (request.getDescription() != null && !request.getDescription().isBlank()) ? request.getDescription() : DESC_DEPOSIT_DEFAULT;
            lastTransaction = createAndSaveSavingDetail(savingAccount, SavingTransactionType.DEPOSIT, MutationType.CREDIT, request.getAmount(), description, savingRef, request.getChannel(), now);
            updateSavingAccountBalance(savingAccount, lastTransaction.getEndBalance(), now);

            if (request.isJournal()) {
               MChartOfAccount coaProduct = findActiveCoaByCode(config.getCoaCode());
                postJournalEntry(savingRef, "SAVING_DEPOSIT", description, transactionDate, request.getAmount(), appCoaConfig.getCashTellerId(), coaProduct.getId(), userMetaData);
            }
        }
        return mapToTransactionResponseDTO(lastTransaction);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO request, UserMetaData userMetaData) {
        validateWithdrawalRequest(request);

        LocalDate transactionDate = Optional.ofNullable(request.getSystemDate()).orElse(LocalDate.now());
        Timestamp now = Timestamp.valueOf(LocalDateTime.of(transactionDate, LocalTime.now()));

        SavingAccount savingAccount = findAndLockSavingAccount(request.getSavingAccountNumber());
        SavingTypeConfig config = getActiveSavingConfig(savingAccount);

        validateAccountIsActive(savingAccount);
        if (savingAccount.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.INSUFFICIENT_BALANCE);
        }
        BigDecimal endBalanceSaving = savingAccount.getCurrentBalance().subtract(request.getAmount());
        validateTransactionRules(savingAccount, config, request.getAmount(), MutationType.DEBIT, endBalanceSaving, transactionDate);

        if (request.isJournal()) {
            findActiveCoaByCode(config.getCoaCode());
        }

        String transactionId = UUID.randomUUID().toString();
        String savingRef = generateSavingTransactionReference(PREFIX_WITHDRAWAL, savingAccount, SavingTransactionType.WITHDRAWAL, transactionId);
        SavingAccountDetail savingDetail = createAndSaveSavingDetail(savingAccount, SavingTransactionType.WITHDRAWAL, MutationType.DEBIT, request.getAmount(), request.getDescription(), savingRef, request.getChannel(), now);
        updateSavingAccountBalance(savingAccount, savingDetail.getEndBalance(), now);

        if (request.isJournal()) {
            MChartOfAccount coaProduct = findActiveCoaByCode(config.getCoaCode());
            postJournalEntry(savingRef, "SAVING_WITHDRAWAL", request.getDescription(), transactionDate, request.getAmount(), coaProduct.getId(), appCoaConfig.getCashTellerId(), userMetaData);
        }

        return mapToTransactionResponseDTO(savingDetail);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SavingTransactionResponseDTO performInternalTransfer(InterBankTransferRequestDTO request, UserMetaData userMetaData) {
        validateInternalTransferRequest(request);
        LocalDate transactionDate = Optional.ofNullable(request.getSystemDate()).orElse(LocalDate.now());
        Timestamp now = Timestamp.valueOf(LocalDateTime.of(transactionDate, LocalTime.now()));

        SavingAccount sourceAccount = findAndLockSavingAccount(request.getSourceAccountNumber());
        SavingAccount destinationAccount = findAndLockSavingAccount(request.getDestinationAccountNumber());

        validateAccountIsActive(sourceAccount);
        validateAccountIsActive(destinationAccount);

        SavingTypeConfig sourceConfig = getActiveSavingConfig(sourceAccount);
        SavingTypeConfig destinationConfig = getActiveSavingConfig(destinationAccount);

        if (sourceAccount.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.INSUFFICIENT_BALANCE);
        }

        BigDecimal finalSourceBalance = sourceAccount.getCurrentBalance().subtract(request.getAmount());
        validateTransactionRules(sourceAccount, sourceConfig, request.getAmount(), MutationType.DEBIT, finalSourceBalance, transactionDate);

        BigDecimal finalDestinationBalance = destinationAccount.getCurrentBalance().add(request.getAmount());
        validateTransactionRules(destinationAccount, destinationConfig, request.getAmount(), MutationType.CREDIT, finalDestinationBalance, transactionDate);

        if (request.isJournal()) {
            findActiveCoaByCode(sourceConfig.getCoaCode());
            findActiveCoaByCode(destinationConfig.getCoaCode());
        }

        String internalTransferReference = "TRF-" + UUID.randomUUID().toString();

        String debitDescription = String.format("Transfer ke %s - %s", destinationAccount.getAccountNumber(), Optional.ofNullable(request.getDescription()).orElse(""));
        SavingAccountDetail debitDetail = createAndSaveSavingDetail(sourceAccount, SavingTransactionType.TRANSFER_INTERNAL_DEBIT, MutationType.DEBIT, request.getAmount(), debitDescription, internalTransferReference, request.getChannel(), now);
        updateSavingAccountBalance(sourceAccount, debitDetail.getEndBalance(), now);

        String creditDescription = String.format("Transfer dari %s - %s", sourceAccount.getAccountNumber(), Optional.ofNullable(request.getDescription()).orElse(""));
        createAndSaveSavingDetail(destinationAccount, SavingTransactionType.TRANSFER_INTERNAL_CREDIT, MutationType.CREDIT, request.getAmount(), creditDescription, internalTransferReference, request.getChannel(), now);
        updateSavingAccountBalance(destinationAccount, finalDestinationBalance, now);

        if (request.isJournal()) {
            MChartOfAccount coaSource = findActiveCoaByCode(sourceConfig.getCoaCode());
            MChartOfAccount coaDestination = findActiveCoaByCode(destinationConfig.getCoaCode());
            postJournalEntry(internalTransferReference, "SAVING_INTERNAL_TRANSFER", request.getDescription(), transactionDate, request.getAmount(), coaSource.getId(), coaDestination.getId(), userMetaData);
        }

        return mapToTransactionResponseDTO(debitDetail);
    }

    private void postJournalEntry(String referenceNumber, String referenceType, String description, LocalDate transactionDate, BigDecimal amount, String debitCoaId, String creditCoaId, UserMetaData userMetaData) {
        JournalDetailRequest debitDetail = new JournalDetailRequest();
        debitDetail.setCoaId(debitCoaId);
        debitDetail.setMutationType(MutationType.DEBIT.name());
        debitDetail.setAmount(amount);
        debitDetail.setDescription(description);

        JournalDetailRequest creditDetail = new JournalDetailRequest();
        creditDetail.setCoaId(creditCoaId);
        creditDetail.setMutationType(MutationType.CREDIT.name());
        creditDetail.setAmount(amount);
        creditDetail.setDescription(description);

        List<JournalDetailRequest> details = new ArrayList<>();
        details.add(debitDetail);
        details.add(creditDetail);

        JournalRequest journalRequest = new JournalRequest();
        journalRequest.setReferenceNumber(referenceNumber);
        journalRequest.setReferenceType(referenceType);
        journalRequest.setDescription(description);
        journalRequest.setSystemDate(transactionDate);
        journalRequest.setDetails(details);

        try {
             journalLedgerService.createJournal(journalRequest, userMetaData);

        } catch (Exception e) {
           throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Transaksi utama berhasil, namun gagal membuat jurnal. Cek log server. Error: " + e.getMessage());
        }
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
                .map(this::mapToTransactionResponseDTO);

        pageResult.getContent().forEach(dto -> dto.setSavingAccountNumber(savingAccount.getAccountNumber()));
        return pageResult;
    }

    private void processOpeningFee(SavingAccount account, SavingTypeConfig config, String relatedReference, Timestamp now, LocalDate transactionDate, boolean isJournal, UserMetaData userMetaData) {
        BigDecimal openingFee = Optional.ofNullable(config.getMonthlyMaintenanceFee()).orElse(BigDecimal.ZERO);
        if (openingFee.compareTo(BigDecimal.ZERO) > 0) {
            if (isJournal) {
                findActiveCoaByCode(config.getCoaCode());
            }

            String feeTransactionReference = PREFIX_FEE + relatedReference;
            SavingAccountDetail feeDetail = createAndSaveSavingDetail(account, SavingTransactionType.FEE_DEBIT, MutationType.DEBIT, openingFee, DESC_OPENING_FEE, feeTransactionReference, CHANNEL_SYSTEM, now);
            updateSavingAccountBalance(account, feeDetail.getEndBalance(), now);

            if (isJournal) {
                MChartOfAccount coaProduct = findActiveCoaByCode(config.getCoaCode());
                postJournalEntry(feeTransactionReference, "SAVING_OPENING_FEE", DESC_OPENING_FEE, transactionDate, openingFee, coaProduct.getId(), appCoaConfig.getFeeIncomeId(), userMetaData);
            }
        }
    }

    private SavingAccount findAndLockSavingAccount(String accountNumber) {
        return savingAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
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
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        return savingAccountDetailRepository.save(detail);
    }

    private void updateSavingAccountBalance(SavingAccount account, BigDecimal newBalance, Timestamp now) {
        account.setCurrentBalance(newBalance);
        account.setLastTransactionAt(now);
        account.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
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

    private MChartOfAccount findActiveCoaByCode(String coaCode) {
        if (coaCode == null || coaCode.isBlank()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Konfigurasi COA Code pada produk tidak boleh kosong.");
        }
        MChartOfAccount coa = mChartOfAccountRepository.findByCode(coaCode)
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Konfigurasi COA tidak valid. Kode tidak ditemukan: " + coaCode));
        if (!coa.getIsActive()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Konfigurasi COA tidak valid. Akun tidak aktif: " + coaCode);
        }
        return coa;
    }

    private String generateSavingTransactionReference(String prefix, SavingAccount account, SavingTransactionType type, String transactionId) {
        String uniqueSuffix = transactionId.substring(0, 8).toUpperCase();
        if (type == SavingTransactionType.INITIAL_DEPOSIT) {
            return String.format("%s-1-%s", prefix, uniqueSuffix);
        }
        long count = savingAccountDetailRepository.countBySavingAccountAndTransactionType(account, type);
        return String.format("%s-%d-%s", prefix, count + 1, uniqueSuffix);
    }

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

    private void validateTransactionRules(SavingAccount account, SavingTypeConfig config, BigDecimal amount, MutationType mutationType, BigDecimal endBalance, LocalDate transactionDate) {
        if (mutationType == MutationType.CREDIT && config.getMaxBalanceLimit() != null && endBalance.compareTo(config.getMaxBalanceLimit()) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.MAX_BALANCE_EXCEEDED);
        }
        if (mutationType == MutationType.DEBIT && config.getMinBalanceLimit() != null && endBalance.compareTo(config.getMinBalanceLimit()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.MIN_BALANCE_VIOLATED);
        }
        validateDailyTransactionLimit(account, amount, mutationType, config.getDailyTransactionLimit(), transactionDate);
        validateDailyTransactionCount(account, config.getDailyTransactionCountLimit(), transactionDate);
    }

    private void validateInitialDepositRules(BigDecimal amount, SavingTypeConfig config) {
        if (config.getMinInitialDeposit() != null && amount.compareTo(config.getMinInitialDeposit()) < 0) {
            String errorMessage = formatErrorMessage(GlobalErrorMapping.MINIMUM_INITIAL_DEPOSIT, config.getMinInitialDeposit().toPlainString());
            throw new BusinessException(HttpStatus.BAD_REQUEST, errorMessage, GlobalErrorMapping.MINIMUM_INITIAL_DEPOSIT.code);
        }
    }

    private void validateDailyTransactionCount(SavingAccount account, Integer dailyCountLimit, LocalDate transactionDate) {
        if (dailyCountLimit == null || dailyCountLimit <= 0) return;
        long transactionCount = countTransactionsByDate(account, transactionDate);
        if (transactionCount >= dailyCountLimit) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.DAILY_COUNT_LIMIT_EXCEEDED);
        }
    }

    private void validateDailyTransactionLimit(SavingAccount account, BigDecimal amount, MutationType type, BigDecimal limit, LocalDate transactionDate) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal sumToday = sumTransactionsByDate(account, type, transactionDate);
        if (sumToday.add(amount).compareTo(limit) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.DAILY_NOMINAL_LIMIT_EXCEEDED);
        }
    }

    private long countTransactionsByDate(SavingAccount account, LocalDate date) {
        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.atTime(LocalTime.MAX));
        return savingAccountDetailRepository.countBySavingAccountAndTransactionAtBetween(account, start, end);
    }

    private BigDecimal sumTransactionsByDate(SavingAccount account, MutationType type, LocalDate date) {
        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.atTime(LocalTime.MAX));
        return Optional.ofNullable(savingAccountDetailRepository.sumTransactionsByAccountAndMutationTypeAndDate(account, type, start, end)).orElse(BigDecimal.ZERO);
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