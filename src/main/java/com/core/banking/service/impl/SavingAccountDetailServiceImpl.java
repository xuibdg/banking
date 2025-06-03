package com.core.banking.service.impl;

import com.core.banking.dto.SavingAccountDetail.AccountStatementRequestDTO;
import com.core.banking.dto.SavingAccountDetail.DepositRequestDTO;
import com.core.banking.dto.SavingAccountDetail.PaginatedResponseDTO;
import com.core.banking.dto.SavingAccountDetail.SavingTransactionResponseDTO;
import com.core.banking.dto.SavingAccountDetail.WithdrawalRequestDTO;
import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingAccountDetail;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.SavingAccountStatus;
import com.core.banking.enums.SavingTransactionType;
import com.core.banking.repository.EscrowAccountRepository;
import com.core.banking.repository.SavingAccountDetailRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.SavingAccountDetailService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.core.banking.utils.exception.GlobalErrorMapping.INVALID_PAGE_SIZE_PARAM;
import static com.core.banking.utils.exception.GlobalErrorMapping.TRX_REF_GENERATION_FAILED;

@Service
@RequiredArgsConstructor // Ini sudah cukup untuk dependency injection jika field final
public class SavingAccountDetailServiceImpl implements SavingAccountDetailService {

    // @Autowired // Tidak wajib jika menggunakan @RequiredArgsConstructor dan field final
    private final SavingAccountRepository savingAccountRepository;
    // @Autowired // Tidak wajib
    private final SavingAccountDetailRepository savingAccountDetailRepository;
    // @Autowired // Tidak wajib
    private final EscrowAccountRepository escrowAccountRepository;


    private static final DateTimeFormatter TRX_REF_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
    private static final Random random = new Random();

    private String formatErrorMessage(GlobalErrorMapping mapping, String... params) {
        String message = mapping.message;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replace("${" + (i + 1) + "}", params[i]);
            }
        }
        return message;
    }

    private String generateTransactionReference(String type) {
        LocalDateTime ldt = LocalDateTime.now();
        String dateTimeStr = ldt.format(TRX_REF_DATETIME_FORMATTER);

        int sequence = random.nextInt(10000);
        String sequenceStr = String.format("%04d", sequence);

        String ref = type + "-" + sequenceStr + "-" + dateTimeStr;

        int maxAttempts = 5;
        int attempt = 0;
        while(savingAccountDetailRepository.findByTransactionReference(ref).isPresent() && attempt < maxAttempts) {
            sequence = random.nextInt(10000);
            sequenceStr = String.format("%04d", sequence);
            ref = type + "-" + sequenceStr + "-" + dateTimeStr;
            attempt++;
        }
        if (attempt >= maxAttempts) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, TRX_REF_GENERATION_FAILED);
        }
        return ref;
    }

    @Override
    @Transactional
    public SavingTransactionResponseDTO recordDeposit(DepositRequestDTO request) {
        validateDepositRequest(request);
        String transactionReference = generateTransactionReference("DEP");

        SavingAccount savingAccount = savingAccountRepository.findWithLockByAccountNumber(request.getSavingAccountNumber())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));

        EscrowAccount sourceEscrowAccount = escrowAccountRepository.findWithLockByAccountNumber(request.getSourceEscrowAccountNumber())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM.code,
                        formatErrorMessage(GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM, "Source Escrow Account " + request.getSourceEscrowAccountNumber()),
                        formatErrorMessage(GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM, "Source Escrow Account " + request.getSourceEscrowAccountNumber())));

        SavingTypeConfig config = savingAccount.getSavingTypeConfig();
        if (config == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalErrorMapping.SAVING_CONFIG_NOT_FOUND);
        }

        if (savingAccount.getAccountStatus() != SavingAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ACCOUNT_NOT_ACTIVE);
        }

        if (sourceEscrowAccount.getAccountStatus() != EscrowAccountStatus.PENDING_FUNDING) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FUNDED);
        }
        if (sourceEscrowAccount.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ESCROW_INSUFFICIENT_BALANCE);
        }

        BigDecimal beginBalance = savingAccount.getCurrentBalance();
        BigDecimal endBalance = beginBalance.add(request.getAmount());

        if (config.getMaxBalanceLimit() != null && endBalance.compareTo(config.getMaxBalanceLimit()) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.MAX_BALANCE_EXCEEDED);
        }

        validateDailyTransactionLimit(savingAccount, request.getAmount(), MutationType.CREDIT, config.getDailyTransactionLimit());

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        sourceEscrowAccount.setCurrentBalance(sourceEscrowAccount.getCurrentBalance().subtract(request.getAmount()));
        sourceEscrowAccount.setUpdatedAt(now);
        escrowAccountRepository.save(sourceEscrowAccount);

        savingAccount.setCurrentBalance(endBalance);
        savingAccount.setLastTransactionAt(now);
        savingAccount.setUpdatedAt(now);
        savingAccountRepository.save(savingAccount);

        SavingAccountDetail detail = SavingAccountDetail.builder()
                .savingAccount(savingAccount)
                .transactionType(SavingTransactionType.DEPOSIT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(request.getAmount())
                .beginBalance(beginBalance)
                .endBalance(endBalance)
                .description(request.getDescription())
                .transactionReference(transactionReference)
                .channel(request.getChannel())
                .transactionAt(now)
                .createdAt(now)
                .build();
        SavingAccountDetail savedDetail = savingAccountDetailRepository.save(detail);

        return mapToTransactionResponseDTO(savedDetail, savingAccount.getAccountNumber());
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SavingTransactionResponseDTO recordWithdrawal(WithdrawalRequestDTO request) {
        validateWithdrawalRequest(request);
        String transactionReference = generateTransactionReference("WD");

        SavingAccount savingAccount = savingAccountRepository.findWithLockByAccountNumber(request.getSavingAccountNumber())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));

        EscrowAccount destinationEscrowAccount = escrowAccountRepository.findWithLockByAccountNumber(request.getDestinationEscrowAccountNumber())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM.code,
                        formatErrorMessage(GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM, "Destination Escrow Account " + request.getDestinationEscrowAccountNumber()),
                        formatErrorMessage(GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM, "Destination Escrow Account " + request.getDestinationEscrowAccountNumber())));

        SavingTypeConfig config = savingAccount.getSavingTypeConfig();
        if (config == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalErrorMapping.SAVING_CONFIG_NOT_FOUND);
        }

        if (savingAccount.getAccountStatus() != SavingAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ACCOUNT_NOT_ACTIVE);
        }

        if (destinationEscrowAccount.getAccountStatus() != EscrowAccountStatus.PENDING_FUNDING) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FUNDED);
        }

        BigDecimal beginBalance = savingAccount.getCurrentBalance();
        if (beginBalance.compareTo(request.getAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.INSUFFICIENT_BALANCE);
        }
        BigDecimal endBalance = beginBalance.subtract(request.getAmount());

        if (config.getMinBalanceLimit() != null && endBalance.compareTo(config.getMinBalanceLimit()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.MIN_BALANCE_VIOLATED);
        }

        validateDailyTransactionLimit(savingAccount, request.getAmount(), MutationType.DEBIT, config.getDailyTransactionLimit());

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        destinationEscrowAccount.setCurrentBalance(destinationEscrowAccount.getCurrentBalance().add(request.getAmount()));
        destinationEscrowAccount.setUpdatedAt(now);
        escrowAccountRepository.save(destinationEscrowAccount);

        savingAccount.setCurrentBalance(endBalance);
        savingAccount.setLastTransactionAt(now);
        savingAccount.setUpdatedAt(now);
        savingAccountRepository.save(savingAccount);

        SavingAccountDetail detail = SavingAccountDetail.builder()
                .savingAccount(savingAccount)
                .transactionType(SavingTransactionType.WITHDRAWAL)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(request.getAmount())
                .beginBalance(beginBalance)
                .endBalance(endBalance)
                .description(request.getDescription())
                .transactionReference(transactionReference)
                .channel(request.getChannel())
                .transactionAt(now)
                .createdAt(now)
                .build();
        SavingAccountDetail savedDetail = savingAccountDetailRepository.save(detail);

        return mapToTransactionResponseDTO(savedDetail, savingAccount.getAccountNumber());
    }


    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<SavingTransactionResponseDTO> getAccountStatement(
            String savingAccountNumber,
            LocalDate startDate, // Sesuai dengan nama parameter di interface
            LocalDate endDate,   // Sesuai dengan nama parameter di interface
            int page,
            int size) {

        // 1. Konversi LocalDate ke Timestamp
        Timestamp startTimestamp = null;
        if (startDate != null) {
            startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        }

        Timestamp endTimestamp = null;
        if (endDate != null) {
            // Untuk endDate, kita ambil sampai akhir hari (23:59:59.999...)
            endTimestamp = Timestamp.valueOf(endDate.atTime(LocalTime.MAX));
        }

        // 2. Buat AccountStatementRequestDTO secara internal
        AccountStatementRequestDTO internalRequestDTO = AccountStatementRequestDTO.builder()
                .savingAccountNumber(savingAccountNumber)
                .startDate(startTimestamp)
                .endDate(endTimestamp)
                .page(page)
                .size(size)
                .build();

        // 3. Validasi menggunakan DTO yang baru dibuat
        validateAccountStatementRequest(internalRequestDTO);

        // 4. Lanjutkan dengan logika yang ada, menggunakan internalRequestDTO
        SavingAccount savingAccount = savingAccountRepository.findByAccountNumber(internalRequestDTO.getSavingAccountNumber())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));

        // Ambil start/end Timestamp dari internalRequestDTO untuk konsistensi
        Timestamp effectiveStartDateTime = internalRequestDTO.getStartDate();
        Timestamp effectiveEndDateTime = internalRequestDTO.getEndDate();

        if (effectiveStartDateTime != null && effectiveEndDateTime != null && effectiveEndDateTime.before(effectiveStartDateTime)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_DATE_RANGE);
        }

        Pageable pageable = PageRequest.of(internalRequestDTO.getPage(), internalRequestDTO.getSize(), Sort.by("transactionAt").descending());

        Page<SavingAccountDetail> pageResult = savingAccountDetailRepository.findBySavingAccountAndDateRange(
                savingAccount.getSavingAccountId(),
                effectiveStartDateTime, // Gunakan Timestamp yang sudah diproses
                effectiveEndDateTime,   // Gunakan Timestamp yang sudah diproses
                pageable
        );

        List<SavingTransactionResponseDTO> transactionDTOs = pageResult.getContent().stream()
                .map(detail -> mapToTransactionResponseDTO(detail, savingAccount.getAccountNumber()))
                .collect(Collectors.toList());

        return PaginatedResponseDTO.<SavingTransactionResponseDTO>builder()
                .content(transactionDTOs)
                .currentPage(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    private void validateDailyTransactionLimit(SavingAccount account, BigDecimal currentTransactionAmount, MutationType mutationType, BigDecimal dailyLimitConfig) {
        if (dailyLimitConfig == null || dailyLimitConfig.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        Timestamp todayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp todayEnd = Timestamp.valueOf(today.atTime(LocalTime.MAX));

        BigDecimal sumToday = savingAccountDetailRepository.sumTransactionsByAccountAndMutationTypeAndDate(
                account, mutationType, todayStart, todayEnd
        );
        BigDecimal totalExistingToday = (sumToday == null) ? BigDecimal.ZERO : sumToday;

        if (totalExistingToday.add(currentTransactionAmount).compareTo(dailyLimitConfig) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.DAILY_NOMINAL_LIMIT_EXCEEDED);
        }
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
                .createdAt(detail.getCreatedAt())
                .build();
    }

    private void validateDepositRequest(DepositRequestDTO request) {
        if (request.getSavingAccountNumber() == null || request.getSavingAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND.code);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_DEPOSIT_AMOUNT);
        }
        if (request.getSourceEscrowAccountNumber() == null || request.getSourceEscrowAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM.code,
                    formatErrorMessage(GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM, "Source Escrow Account Number"),
                    "Source Escrow account number is required for deposit.");
        }
    }

    private void validateWithdrawalRequest(WithdrawalRequestDTO request) {
        if (request.getSavingAccountNumber() == null || request.getSavingAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND.code);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_WITHDRAWAL_AMOUNT);
        }
        if (request.getDestinationEscrowAccountNumber() == null || request.getDestinationEscrowAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM.code,
                    formatErrorMessage(GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM, "Destination Escrow Account Number"),
                    "Destination Escrow account number is required for withdrawal.");
        }
    }

    private void validateAccountStatementRequest(AccountStatementRequestDTO request) {
        if (request.getSavingAccountNumber() == null || request.getSavingAccountNumber().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.MISSING_ACCOUNT_ID);
        }
        if (request.getPage() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,GlobalErrorMapping.INVALID_PAGE_PARAM);
        }
        if (request.getSize() <= 0 || request.getSize() > 100) { // Batas atas 100 seperti di controller
            throw new BusinessException(HttpStatus.BAD_REQUEST, INVALID_PAGE_SIZE_PARAM);
        }
    }
}