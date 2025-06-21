package com.core.banking.service.impl;

import com.core.banking.dto.DepositMaturityResponse;
import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.*;
import com.core.banking.enums.*;
import com.core.banking.repository.DepositAccountDetailRepository;
import com.core.banking.repository.DepositAccountRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.DepositMaturityService;
import com.core.banking.service.EscrowAccountDetailService;
import com.core.banking.utils.DepositAccountNumberGenerator;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class DepositMaturityServiceImpl implements DepositMaturityService {
    @Autowired
    DepositAccountRepository depositAccountRepository;

    @Autowired
    DepositAccountDetailRepository depositAccountDetailRepository;

    @Autowired
    DepositAccountNumberGenerator depositAccountNumberGenerator;

    @Autowired
    SavingAccountRepository savingAccountRepository;

    @Autowired
    EscrowAccountDetailService escrowAccountDetailService;

    @Override
    @Transactional
    public DepositMaturityResponse processMaturity(Long depositoAccountId, String savingAccountId, UserMetaData userMetaData) {
        DepositAccount depositAccount = depositAccountRepository.findById(depositoAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
        SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
        //TODO: TAMBAH VALIDASI SAVING ACCOUNT ID

        if (!depositAccount.getCustomer().getId().equals(savingAccount.getCustomer().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_CUSTOMER_INEQUAL); //TODO: MAXIMIZE GLOBALERRORMAPPING
        }

        if (savingAccount.getAccountStatus() != SavingAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_ACTIVE);
        }

        if (depositAccount.getAccountStatus() != DepositAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_ACTIVE);
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(depositAccount.getMaturityDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.MATURITY_DATE_NOT_REACHED);
        }

        BigDecimal profit = calculateProfit(depositAccount);
        BigDecimal total = depositAccount.getPrincipalAmount().add(profit);

        RolloverOption rolloverOption = depositAccount.getRolloverOption();
        DepositAccountStatus oldStatus = depositAccount.getAccountStatus();
        DepositMaturityResponse depositMaturityResponse = DepositMaturityResponse.builder()
                .depositAccountId(depositoAccountId)
                .accountNumber(depositAccount.getAccountNumber())
                .customerName(depositAccount.getCustomer().getFullName())
                .principalAmount(depositAccount.getPrincipalAmount())
                .profitAmount(profit)
                .totalAmount(total)
                .maturityDate(depositAccount.getMaturityDate())
                .beforeStatus(oldStatus)
                .rolloverOption(rolloverOption.name())
                .success(true)
                .build();

        switch (rolloverOption) {
            case NO_ROLLOVER:
                depositPayout(depositAccount, profit, savingAccount, userMetaData);
                depositMaturityResponse.setAfterStatus(DepositAccountStatus.MATURED_PAID);
                depositMaturityResponse.setMessage("Akun Deposito Mudharabah telah sukses dibayar");
                break;

            case PRINCIPAL_ONLY:
                DepositAccount newAccountPrincipalOnly = depositRolloverPrincipalOnly(depositAccount, profit);
                depositMaturityResponse.setAfterStatus(DepositAccountStatus.ROLLED_OVER);
                depositMaturityResponse.setNewDepositAccountId(newAccountPrincipalOnly.getDepositoAccountId());
                depositMaturityResponse.setNewAccountNumber(newAccountPrincipalOnly.getAccountNumber());
                depositMaturityResponse.setMessage("Akun Deposito Mudharabah telah sukses diperpanjang (Hanya dana pokok)");
                break;

            case PRINCIPAL_AND_PROFIT:
                DepositAccount newAccountPrincipalAndProfit = depositRolloverPrincipalAndProfit(depositAccount, profit);
                depositMaturityResponse.setAfterStatus(DepositAccountStatus.ROLLED_OVER);
                depositMaturityResponse.setNewDepositAccountId(newAccountPrincipalAndProfit.getDepositoAccountId());
                depositMaturityResponse.setNewAccountNumber(newAccountPrincipalAndProfit.getAccountNumber());
                depositMaturityResponse.setMessage("Akun Deposito Mudharabah telah sukses diperpanjang (Dana pokok dan keuntungan)");
                break;

            default:
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_ROLLOVER_OPTION);
        }
        return depositMaturityResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositMaturityResponse> getAllMaturedDeposits(LocalDate maturityDate) {
        if (maturityDate == null) {
            maturityDate = LocalDate.now();
        }

        List<DepositAccount> maturedAccounts = depositAccountRepository.findByAccountStatusAndMaturityDateLessThanEqual(DepositAccountStatus.ACTIVE, maturityDate);

        List<DepositMaturityResponse> list = maturedAccounts.stream().map(data -> {
            try {
                BigDecimal profit = calculateProfit(data);
                BigDecimal total = data.getPrincipalAmount().add(profit);

                return DepositMaturityResponse.builder()
                        .depositAccountId(data.getDepositoAccountId())
                        .accountNumber(data.getAccountNumber())
                        .customerName(data.getCustomer().getFullName())
                        .principalAmount(data.getPrincipalAmount())
                        .profitAmount(profit)
                        .totalAmount(total)
                        .maturityDate(data.getMaturityDate())
                        .beforeStatus(data.getAccountStatus())
                        .rolloverOption(data.getRolloverOption().name())
                        .message("Akun telah siap untuk diproses")
                        .success(true)
                        .build();
            } catch (Exception e) {
                return DepositMaturityResponse.builder()
                        .depositAccountId(data.getDepositoAccountId())
                        .accountNumber(data.getAccountNumber())
                        .customerName(data.getCustomer().getFullName())
                        .success(false)
                        .message("Data dengan akun diatas mengalami error dengan detail: " + e.getMessage())
                        .build();
            }
        }).collect(Collectors.toList());
        return list;
    }

    private BigDecimal calculateProfit(DepositAccount depositAccount) {
        DepositTypeConfig depositTypeConfig = depositAccount.getDepositTypeConfig();
        BigDecimal principal = depositAccount.getPrincipalAmount();

        BigDecimal expectedProfit = calculateExpectedProfit(principal, depositAccount);
        BigDecimal customerRatio = depositTypeConfig.getProfitSharingRatioCustomer().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal profit = expectedProfit.multiply(customerRatio).setScale(2, RoundingMode.HALF_UP);

        return profit;
    }

    public BigDecimal calculateExpectedProfit(BigDecimal principal, DepositAccount depositAccount) {
        DepositTypeConfig depositTypeConfig = depositAccount.getDepositTypeConfig();

        BigDecimal expectedRate = depositTypeConfig.getProfitSharePercentagePa().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        LocalDate openDate = depositAccount.getOpenedAt().toLocalDate();
        LocalDate maturityDate = depositAccount.getMaturityDate();
        long daysInTerm = ChronoUnit.DAYS.between(openDate, maturityDate);

        BigDecimal daysInYear = new BigDecimal(365);
        BigDecimal expectedProfit = principal.multiply(expectedRate).multiply(new BigDecimal(daysInTerm).divide(daysInYear, 10, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);

        return expectedProfit;
    }

    private void depositPayout(DepositAccount depositAccount, BigDecimal profit, SavingAccount savingAccount, UserMetaData userMetaData) {
        DepositAccountDetail profitDetail = DepositAccountDetail.builder()
                .depositAccount(depositAccount)
                .transactionType(DepositoTransactionType.PROFIT_PAYOUT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(profit)
                .beginBalance(depositAccount.getPrincipalAmount())
                .endBalance(depositAccount.getPrincipalAmount())
                .description("Pembayaran bagi hasil Mudharabah pada saat jatuh tempo")
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(profitDetail);

        DepositAccountDetail principalDetail = DepositAccountDetail.builder()
                .depositAccount(depositAccount)
                .transactionType(DepositoTransactionType.MATURITY_WITHDRAWAL)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(depositAccount.getPrincipalAmount())
                .beginBalance(depositAccount.getPrincipalAmount())
                .endBalance(BigDecimal.ZERO)
                .description("Penarikan dana pokok Mudharabah pada saat jatuh tempo")
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(principalDetail);

        BigDecimal totalPayout = depositAccount.getPrincipalAmount().add(profit);

        EscrowAccountRequest escrowRequest = new EscrowAccountRequest();
        escrowRequest.setPayerCustomer(depositAccount.getCustomer().getId());
        escrowRequest.setBeneficiaryCustomer(depositAccount.getCustomer().getId());
        escrowRequest.setTransactionTypeStatus(TransactionTypeStatus.DEPOSIT_PAYMENT);
        escrowRequest.setDepositAccount(depositAccount.getDepositoAccountId());
        escrowRequest.setPurpose("Pencairan Jatuh Tempo Deposito");

        String transactionReference = escrowAccountDetailService.createAndReleaseEscrowAccount(escrowRequest, totalPayout, savingAccount.getAccountNumber(), "Pencairan Jatuh Tempo Deposito " + depositAccount.getAccountNumber(), userMetaData);

        principalDetail.setTransactionReference(transactionReference);
        depositAccountDetailRepository.save(principalDetail);

        depositAccount.setAccountStatus(DepositAccountStatus.MATURED_PAID);
        depositAccount.setClosedAt(LocalDateTime.now());
        depositAccountRepository.save(depositAccount);
    }

    private DepositAccount depositRolloverPrincipalOnly(DepositAccount oldAccount, BigDecimal profit) {
        DepositAccountDetail profitDetail = DepositAccountDetail.builder()
                .depositAccount(oldAccount)
                .transactionType(DepositoTransactionType.PROFIT_PAYOUT)
                .nominalTransaction(profit)
                .mutationType(MutationType.CREDIT)
                .beginBalance(oldAccount.getPrincipalAmount())
                .endBalance(oldAccount.getPrincipalAmount())
                .description("Pembayaran bagi hasil Mudharabah pada saat rollover/perpanjangan")
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(profitDetail);

        DepositAccountDetail rolloverDetail = DepositAccountDetail.builder()
                .depositAccount(oldAccount)
                .transactionType(DepositoTransactionType.ROLLOVER_PRINCIPAL)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(oldAccount.getPrincipalAmount())
                .beginBalance(oldAccount.getPrincipalAmount())
                .endBalance(BigDecimal.ZERO)
                .description("Perpanjangan dana pokok pada saat jatuh tempo")
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(rolloverDetail);

        oldAccount.setAccountStatus(DepositAccountStatus.ROLLED_OVER);
        oldAccount.setClosedAt(LocalDateTime.now());
        depositAccountRepository.save(oldAccount);

        String accountNumber = depositAccountNumberGenerator.generateDepositAccountNumber();
        DepositTypeConfig depositTypeConfig = oldAccount.getDepositTypeConfig();
        Customer customer = oldAccount.getCustomer();

        LocalDate newMaturityDate = LocalDate.now().plusMonths(depositTypeConfig.getTermInMonths());

        DepositAccount newAccount = DepositAccount.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .depositTypeConfig(depositTypeConfig)
                .principalAmount(oldAccount.getPrincipalAmount())
                .maturityDate(newMaturityDate)
                .accountStatus(DepositAccountStatus.ACTIVE)
                .rolloverOption(oldAccount.getRolloverOption())
                .openedAt(LocalDateTime.now())
//                .isDeleted(false)
                .build();

        DepositAccount savedNewAccount = depositAccountRepository.save(newAccount);

        DepositAccountDetail initialDetail = DepositAccountDetail.builder()
                .depositAccount(savedNewAccount)
                .transactionType(DepositoTransactionType.INITIAL_DEPOSIT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(oldAccount.getPrincipalAmount())
                .beginBalance(BigDecimal.ZERO)
                .endBalance(oldAccount.getPrincipalAmount())
                .description("Inisialisasi Setoran awal dari rollover akun " + oldAccount.getAccountNumber())
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(initialDetail);

        return savedNewAccount;
    }

    private DepositAccount depositRolloverPrincipalAndProfit(DepositAccount oldAccount, BigDecimal profit) {
        BigDecimal total = oldAccount.getPrincipalAmount().add(profit);

        DepositAccountDetail profitDepositAccountDetail = DepositAccountDetail.builder()
                .depositAccount(oldAccount)
                .transactionType(DepositoTransactionType.PROFIT_PAYOUT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(profit)
                .beginBalance(oldAccount.getPrincipalAmount())
                .endBalance(total)
                .description("Perhitungan bagi hasil Mudharabah untuk perpanjangan")
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(profitDepositAccountDetail);

        DepositAccountDetail rolloverDepositAccountDetail = DepositAccountDetail.builder()
                .depositAccount(oldAccount)
                .transactionType(DepositoTransactionType.ROLLOVER_PROFIT)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(total)
                .beginBalance(total)
                .endBalance(BigDecimal.ZERO)
                .description("Perpanjangan dana pokok dan keuntungan pada saat jatuh tempo")
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(rolloverDepositAccountDetail);

        oldAccount.setAccountStatus(DepositAccountStatus.ROLLED_OVER);
        oldAccount.setClosedAt(LocalDateTime.now());
        depositAccountRepository.save(oldAccount);

        String accountNumber = depositAccountNumberGenerator.generateDepositAccountNumber();
        DepositTypeConfig depositTypeConfig = oldAccount.getDepositTypeConfig();
        Customer customer = oldAccount.getCustomer();

        LocalDate newMaturityDate = LocalDate.now().plusMonths(depositTypeConfig.getTermInMonths());

        DepositAccount newDepositAccount = DepositAccount.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .depositTypeConfig(depositTypeConfig)
                .principalAmount(total)
                .maturityDate(newMaturityDate)
                .accountStatus(DepositAccountStatus.ACTIVE)
                .rolloverOption(oldAccount.getRolloverOption())
                .openedAt(LocalDateTime.now())
//                .isDeleted(false)
                .build();

        DepositAccount savedNewAccount = depositAccountRepository.save(newDepositAccount);

        DepositAccountDetail initialDetail = DepositAccountDetail.builder()
                .depositAccount(savedNewAccount)
                .transactionType(DepositoTransactionType.INITIAL_DEPOSIT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(total)
                .beginBalance(BigDecimal.ZERO)
                .endBalance(total)
                .description("Setoran awal rollover dengan akun " + oldAccount.getAccountNumber())
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(initialDetail);

        return savedNewAccount;
    }
}
