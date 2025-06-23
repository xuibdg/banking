package com.core.banking.service.impl;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private JournalLedgerServiceImpl journalLedgerServiceImpl;

    private static final String DESC_INITIAL_DEPOSIT_TELLER = "Initial Deposit Melalui Teller";
    private static final String DESC_DEPOSIT_DEFAULT = "Deposit";
    private static final String DESC_OPENING_FEE = "Biaya Pembukaan Rekening";
    private static final String CHANNEL_SYSTEM = "SYSTEM";
    private static final String PREFIX_FEE = "FEE-";
    private static final String CASH_TELLER_COA_CODE = "1001";
    private static final String FEE_INCOME_COA_CODE = "4101";



    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public SavingTransactionResponseDTO recordDeposit(DepositRequestDTO request, UserMetaData userMetaData) {
        validateUserMetaData(userMetaData);
        validateDepositRequest(request);

        LocalDate systemDate = journalLedgerServiceImpl.getSystemAt();
        SavingAccount savingAccount = findAndLockSavingAccount(request.getSavingAccountNumber());
        SavingTypeConfig config = getActiveSavingConfig(savingAccount);
        boolean isInitialDeposit = !savingAccountDetailRepository.existsBySavingAccount(savingAccount);

        if (request.isJournal()) {
            findActiveCoaByCode(config.getCoaCode());
            findActiveCoaByCode(CASH_TELLER_COA_CODE);
        }

        if (isInitialDeposit) {
            validateInitialDepositState(savingAccount);
            validateInitialDepositRules(request.getAmount(), config);
        } else {
            validateAccountIsActive(savingAccount);
            BigDecimal endBalanceSaving = savingAccount.getCurrentBalance().add(request.getAmount());
            validateTransactionRules(savingAccount, config, request.getAmount(), MutationType.CREDIT, endBalanceSaving, systemDate);
        }

        String sharedReference = journalLedgerService.generateNewTransactionReference();
        Timestamp now = Timestamp.valueOf(LocalDateTime.of(systemDate, LocalTime.now()));
        SavingTransactionType transactionType = isInitialDeposit ? SavingTransactionType.INITIAL_DEPOSIT : SavingTransactionType.DEPOSIT;
        String description = isInitialDeposit ? DESC_INITIAL_DEPOSIT_TELLER : Optional.ofNullable(request.getDescription()).filter(s -> !s.isBlank()).orElse(DESC_DEPOSIT_DEFAULT);

        SavingAccountDetail lastTransaction = executeTransaction(savingAccount, transactionType, MutationType.CREDIT, request.getAmount(), description, sharedReference, request.getChannel(), now, systemDate);

        if (request.isJournal()) {
            MChartOfAccount coaProduct = findActiveCoaByCode(config.getCoaCode());
            MChartOfAccount coaCashTeller = findActiveCoaByCode(CASH_TELLER_COA_CODE);
            postJournalEntry(sharedReference, transactionType.name(), description, systemDate, request.getAmount(), String.valueOf(Objects.requireNonNull(coaCashTeller).getId()), String.valueOf(Objects.requireNonNull(coaProduct).getId()), userMetaData);
        }
        if (isInitialDeposit) {
            processOpeningFee(savingAccount, config, sharedReference, now, systemDate, request.isJournal(), userMetaData);
            activateAccount(savingAccount, now);
        }

        return mapToTransactionResponseDTO(lastTransaction);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = BusinessException.class)
    public SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO request, UserMetaData userMetaData) {
        validateUserMetaData(userMetaData);
        validateWithdrawalRequest(request);

        LocalDate systemDate = LocalDate.now();
        SavingAccount savingAccount = findAndLockSavingAccount(request.getSavingAccountNumber());
        SavingTypeConfig config = getActiveSavingConfig(savingAccount);

        if (request.isJournal()) {
            findActiveCoaByCode(config.getCoaCode());
            findActiveCoaByCode(CASH_TELLER_COA_CODE);
        }

        validateAccountIsActive(savingAccount);
        if (savingAccount.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.INSUFFICIENT_BALANCE);
        }
        BigDecimal endBalanceSaving = savingAccount.getCurrentBalance().subtract(request.getAmount());
        validateTransactionRules(savingAccount, config, request.getAmount(), MutationType.DEBIT, endBalanceSaving, systemDate);

        String sharedReference = journalLedgerService.generateNewTransactionReference();
        Timestamp now = Timestamp.valueOf(LocalDateTime.of(systemDate, LocalTime.now()));

        SavingAccountDetail savingDetail = executeTransaction(savingAccount, SavingTransactionType.WITHDRAWAL, MutationType.DEBIT, request.getAmount(), request.getDescription(), sharedReference, request.getChannel(), now, systemDate);

        if (request.isJournal()) {
            MChartOfAccount coaProduct = findActiveCoaByCode(config.getCoaCode());
            MChartOfAccount coaCashTeller = findActiveCoaByCode(CASH_TELLER_COA_CODE);
            postJournalEntry(sharedReference, "SAVING_WITHDRAWAL", request.getDescription(), systemDate, request.getAmount(), String.valueOf(Objects.requireNonNull(coaProduct).getId()), String.valueOf(Objects.requireNonNull(coaCashTeller).getId()), userMetaData);
        }

        return mapToTransactionResponseDTO(savingDetail);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = BusinessException.class)
    public SavingTransactionResponseDTO performInternalTransfer(InterBankTransferRequestDTO request, UserMetaData userMetaData) {

        validateUserMetaData(userMetaData);
        validateInternalTransferRequest(request);

        LocalDate systemDate = LocalDate.now();
        SavingAccount sourceAccount = findAndLockSavingAccount(request.getSourceAccountNumber());
        SavingAccount destinationAccount = findAndLockSavingAccount(request.getDestinationAccountNumber());

        validateAccountsForTransfer(sourceAccount, destinationAccount, request.getAmount(), systemDate);

        String sharedReference = "INT-TRF-" + System.currentTimeMillis();
        Timestamp now = Timestamp.valueOf(LocalDateTime.of(systemDate, LocalTime.now()));

        String debitDescription = String.format("Transfer ke %s - %s", destinationAccount.getAccountNumber(), Optional.ofNullable(request.getDescription()).orElse(""));
        SavingAccountDetail debitDetail = executeTransaction(sourceAccount, SavingTransactionType.TRANSFER_INTERNAL_DEBIT, MutationType.DEBIT, request.getAmount(), debitDescription, sharedReference, request.getChannel(), now, systemDate);

        String creditDescription = String.format("Transfer dari %s - %s", sourceAccount.getAccountNumber(), Optional.ofNullable(request.getDescription()).orElse(""));
        executeTransaction(destinationAccount, SavingTransactionType.TRANSFER_INTERNAL_CREDIT, MutationType.CREDIT, request.getAmount(), creditDescription, sharedReference, request.getChannel(), now, systemDate);


        return mapToTransactionResponseDTO(debitDetail);
    }

    // ... getAccountStatement() tidak berubah ...

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

        return savingAccountDetailRepository.findBySavingAccountAndDateRange(savingAccount.getSavingAccountId(), startTimestamp, endTimestamp, pageable)
                .map(detail -> mapToTransactionResponseDTO(detail, savingAccount.getAccountNumber()));
    }


    // BUSINESS LOGIC

    private SavingAccountDetail executeTransaction(SavingAccount account, SavingTransactionType trxType, MutationType mutation, BigDecimal amount, String desc, String ref, String channel, Timestamp now, LocalDate systemDate) {
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
                .channel(Optional.ofNullable(channel).orElse(CHANNEL_SYSTEM))
                .transactionAt(now)
                .systemDate(systemDate)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        savingAccountDetailRepository.save(detail);
        updateSavingAccountBalance(account, endBalance, now);
        return detail;
    }

    private void processOpeningFee(SavingAccount account, SavingTypeConfig config, String relatedReference, Timestamp now, LocalDate systemDate, boolean isJournal, UserMetaData userMetaData) {
        BigDecimal openingFee = Optional.ofNullable(config.getMinInitialDeposit()).orElse(BigDecimal.ZERO);

        if (openingFee.compareTo(BigDecimal.ZERO) > 0) {
            MChartOfAccount coaProduct = null;
            MChartOfAccount coaFeeIncome = null;
            if (isJournal) {
                coaProduct = findActiveCoaByCode(config.getCoaCode());
                coaFeeIncome = findActiveCoaByCode(FEE_INCOME_COA_CODE);
            }

            String feeTransactionReference = PREFIX_FEE + relatedReference;
            executeTransaction(account, SavingTransactionType.FEE_DEBIT, MutationType.DEBIT, openingFee, DESC_OPENING_FEE, feeTransactionReference, CHANNEL_SYSTEM, now, systemDate);

            if (isJournal) {
                postJournalEntry(feeTransactionReference, "SAVING_OPENING_FEE", DESC_OPENING_FEE, systemDate, openingFee, String.valueOf(Objects.requireNonNull(coaProduct).getId()), String.valueOf(Objects.requireNonNull(coaFeeIncome).getId()), userMetaData);
            }
        }
    }

    private void postJournalEntry(String referenceNumber, String referenceType, String description, LocalDate systemDate, BigDecimal amount, String debitCoaId, String creditCoaId, UserMetaData userMetaData) {
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

        List<JournalDetailRequest> details = List.of(debitDetail, creditDetail);

        JournalRequest journalRequest = new JournalRequest();
        journalRequest.setReferenceNumber(referenceNumber);
        journalRequest.setReferenceType(referenceType);
        journalRequest.setDescription(description);
        journalRequest.setSystemDate(systemDate);
        journalRequest.setDetails(details);

        try {
            journalLedgerService.createJournal(journalRequest, userMetaData);
        } catch (Exception e) {
            String pesanErrorAsli = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "JOURNAL_CREATION_FAILED", "Transaksi utama berhasil, namun gagal membuat jurnal. Silakan hubungi admin.", pesanErrorAsli);
        }
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


    // VALIDASI

    private void validateUserMetaData(UserMetaData userMetaData) {
        if (userMetaData == null || userMetaData.getUserId() == null || userMetaData.getUserId().isBlank()) {
            //throw new BusinessException(HttpStatus.UNAUTHORIZED, "USER_IDENTITY_MISSING", "Identitas pengguna tidak valid.", "User ID is missing in metadata.");
        }
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

    private void validateInternalTransferRequest(InterBankTransferRequestDTO request) {
        if (request.getSourceAccountNumber() == null || request.getSourceAccountNumber().isBlank() ||
                request.getDestinationAccountNumber() == null || request.getDestinationAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
        }
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SOURCE_AND_DESTINATION_CANT_BE_THE_SAME);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_NOMINAL_INVALID);
        }
    }

    private void validateAccountsForTransfer(SavingAccount source, SavingAccount destination, BigDecimal amount, LocalDate date) {
        validateAccountIsActive(source);
        validateAccountIsActive(destination);

        SavingTypeConfig sourceConfig = getActiveSavingConfig(source);
        BigDecimal finalSourceBalance = source.getCurrentBalance().subtract(amount);
        validateTransactionRules(source, sourceConfig, amount, MutationType.DEBIT, finalSourceBalance, date);

        SavingTypeConfig destConfig = getActiveSavingConfig(destination);
        BigDecimal finalDestBalance = destination.getCurrentBalance().add(amount);
        validateTransactionRules(destination, destConfig, amount, MutationType.CREDIT, finalDestBalance, date);
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

    private void validateAccountIsActive(SavingAccount account) {
        if (account.getAccountStatus() != SavingAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ACCOUNT_NOT_ACTIVE);
        }
    }

    private void validateInitialDepositState(SavingAccount account) {
        if (account.getAccountStatus() == SavingAccountStatus.CLOSED || account.getAccountStatus() == SavingAccountStatus.DORMANT) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ACCOUNT_NOT_ACTIVE);
        }
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.SAVING_ACCOUNT_HAS_TRANSACTION);
        }
    }

    private void validateInitialDepositRules(BigDecimal amount, SavingTypeConfig config) {
        if (config.getMinInitialDeposit() != null && amount.compareTo(config.getMinInitialDeposit()) < 0) {
            String errorMessage = formatErrorMessage(GlobalErrorMapping.MINIMUM_INITIAL_DEPOSIT, config.getMinInitialDeposit().toPlainString());
            throw new BusinessException(HttpStatus.BAD_REQUEST, errorMessage, GlobalErrorMapping.MINIMUM_INITIAL_DEPOSIT.code);
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

    // HELPER METHOD

    private SavingAccount findAndLockSavingAccount(String accountNumber) {
        return savingAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
    }

    private SavingTypeConfig getActiveSavingConfig(SavingAccount account) {
        SavingTypeConfig config = account.getSavingTypeConfig();
        if (config == null || !config.getIsActive()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalErrorMapping.SAVING_CONFIG_NOT_FOUND);
        }
        return config;
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

    private SavingTransactionResponseDTO mapToTransactionResponseDTO(SavingAccountDetail detail) {
        return mapToTransactionResponseDTO(detail, detail.getSavingAccount().getAccountNumber());
    }

    private SavingTransactionResponseDTO mapToTransactionResponseDTO(SavingAccountDetail detail, String accountNumber) {
        return SavingTransactionResponseDTO.builder()
                .transactionId(detail.getSavingAccountDetailId())
                .savingAccountNumber(accountNumber)
                .transactionType(detail.getTransactionType())
                .mutationType(detail.getMutationType())
                .amount(detail.getNominalTransaction())
                .balanceBefore(detail.getBeginBalance())
                .balanceAfter(detail.getEndBalance())
                .description(detail.getDescription())
                .transactionReference(detail.getTransactionReference())
                .channel(detail.getChannel())
                .transactionTimestamp(detail.getTransactionAt())
                .systemDate(detail.getSystemDate())
                .createdAt(detail.getCreatedAt())
                .build();
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
}