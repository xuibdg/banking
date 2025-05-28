package com.core.banking.service.impl;

import com.core.banking.dto.DepositMaturityResponse;
import com.core.banking.entity.Customer;
import com.core.banking.entity.DepositAccount;
import com.core.banking.entity.DepositAccountDetail;
import com.core.banking.entity.DepositTypeConfig;
import com.core.banking.enums.DepositAccountStatus;
import com.core.banking.enums.DepositoTransactionType;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.RolloverOption;
import com.core.banking.repository.DepositAccountDetailRepository;
import com.core.banking.repository.DepositAccountRepository;
import com.core.banking.repository.DepositProfitSharingRepository;
import com.core.banking.service.DepositMaturityService;
import com.core.banking.utils.DepositAccountNumberGenerator;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DepositMaturityServiceImpl implements DepositMaturityService {
    @Autowired
    DepositAccountRepository depositAccountRepository;

    @Autowired
    DepositAccountDetailRepository depositAccountDetailRepository;

    @Autowired
    DepositProfitSharingRepository depositProfitSharingRepository;

    @Autowired
    DepositAccountNumberGenerator depositAccountNumberGenerator;

    @Override
    @Transactional
    public DepositMaturityResponse depositMaturedDeposit(Long depositoAccountId) {
        DepositAccount depositAccount = depositAccountRepository.findById(depositoAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));

        if (depositAccount.getAccountStatus() != DepositAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_ACTIVE);
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(depositAccount.getMaturityDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR); // TODO: MAXIMIZE GLOBALERRORMAPPING
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
                .previousStatus(oldStatus)
                .rolloverOption(rolloverOption.toString())
                .success(true)
                .build();

        switch (rolloverOption) {
            case NO_ROLLOVER:
                processPayout(depositAccount, profit);
                depositMaturityResponse.setCurrentStatus(DepositAccountStatus.MATURED_PAID);
                depositMaturityResponse.setMessage("Akun Deposit telah sukses dibayar");
                break;

            case PRINCIPAL_ONLY:
                DepositAccount newAccountPrincipalOnly = depositRolloverPrincipalOnly(depositAccount, profit);
                depositMaturityResponse.setCurrentStatus(DepositAccountStatus.ROLLED_OVER);
                depositMaturityResponse.setNewDepositAccountId(newAccountPrincipalOnly.getDepositoAccountId());
                depositMaturityResponse.setNewAccountNumber(newAccountPrincipalOnly.getAccountNumber());
                depositMaturityResponse.setMessage("Akun deposit telah sukses diperpanjang (Hanya dana pokok)");
                break;

            case PRINCIPAL_AND_PROFIT:
                DepositAccount newAccountPrincipalAndProfit = depositRolloverPrincipalAndProfit(depositAccount, profit);
                depositMaturityResponse.setCurrentStatus(DepositAccountStatus.ROLLED_OVER);
                depositMaturityResponse.setNewDepositAccountId(newAccountPrincipalAndProfit.getDepositoAccountId());
                depositMaturityResponse.setNewAccountNumber(newAccountPrincipalAndProfit.getAccountNumber());
                depositMaturityResponse.setMessage("Akun deposito telah sukses diperpanjang (Dana pokok dan keuntungan)");
                break;

            default:
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR); //TODO: MAXIMIZE GLOBAL ERROR MAPPING
        }
        return depositMaturityResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositMaturityResponse> getMaturedDeposits(LocalDate maturityDate) {
        if (maturityDate == null) {
            maturityDate = LocalDate.now();
        }

        List<DepositAccount> maturedAccounts = depositAccountRepository.findByAccountStatusAndMaturityDateLessThanEqual(DepositAccountStatus.ACTIVE, maturityDate);

        List<DepositMaturityResponse> depositMaturityResponses = new ArrayList<>();

        for (DepositAccount account : maturedAccounts) {
            try {
                BigDecimal profit = calculateProfit(account);
                BigDecimal total = account.getPrincipalAmount().add(profit);

                DepositMaturityResponse get = DepositMaturityResponse.builder()
                        .depositAccountId(account.getDepositoAccountId())
                        .accountNumber(account.getAccountNumber())
                        .customerName(account.getCustomer().getFullName())
                        .principalAmount(account.getPrincipalAmount())
                        .profitAmount(profit)
                        .totalAmount(total)
                        .maturityDate(account.getMaturityDate())
                        .previousStatus(account.getAccountStatus())
                        .rolloverOption(account.getRolloverOption().toString())
                        .message("Deposit telah siap untuk diproses")
                        .success(true)
                        .build();
                depositMaturityResponses.add(get);
            } catch (Exception e) {
                DepositMaturityResponse errorResponse = DepositMaturityResponse.builder()
                        .depositAccountId(account.getDepositoAccountId())
                        .accountNumber(account.getAccountNumber())
                        .customerName(account.getCustomer().getFullName())
                        .success(false)
                        .message("Error dengan detail: " + e.getMessage())
                        .build();
                depositMaturityResponses.add(errorResponse);
            }
        }
        return depositMaturityResponses;
    }

    private BigDecimal calculateProfit(DepositAccount depositAccount) {
        DepositTypeConfig depositTypeConfig = depositAccount.getDepositTypeConfig();
        BigDecimal principal = depositAccount.getPrincipalAmount();
        BigDecimal ratePerYear = depositTypeConfig.getProfitSharePercentagePa().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        LocalDate openDate = depositAccount.getOpenedAt().toLocalDate();
        LocalDate maturityDate = depositAccount.getMaturityDate();
        long daysInTerm = ChronoUnit.DAYS.between(openDate, maturityDate);

        BigDecimal daysInYear = new BigDecimal(365);
        BigDecimal dailyRate = ratePerYear.divide(daysInYear, 10, RoundingMode.HALF_UP);
        BigDecimal profit = principal.multiply(dailyRate).multiply(new BigDecimal(daysInTerm)).setScale(2, RoundingMode.HALF_UP);

        return profit;
    }
    private void depositPayout(DepositAccount depositAccount, BigDecimal profit) {
        DepositAccountDetail profitDetail = DepositAccountDetail.builder()
                .depositAccount(depositAccount)
                .transactionType(DepositoTransactionType.PROFIT_PAYOUT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(profit)
                .beginBalance(depositAccount.getPrincipalAmount())
                .endBalance(depositAccount.getPrincipalAmount())
                .description("Pembayaran keuntungan pada saat jatuh tempo")
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
                .description("Penarikan dana pokok pada saat jatuh tempo")
                .transactionAt(LocalDateTime.now())
                .build();
        depositAccountDetailRepository.save(principalDetail);

        depositAccount.setAccountStatus(DepositAccountStatus.MATURED_PAID);
        depositAccount.setClosedAt(LocalDateTime.now());
        depositAccountRepository.save(depositAccount);
    }

    private DepositAccountDetail depositRolloverPrincipalOnly(DepositAccount oldAccount, BigDecimal profit) {
        DepositAccountDetail profitDetail = DepositAccountDetail.builder()
                .depositAccount(oldAccount)
                .transactionType(DepositoTransactionType.PROFIT_PAYOUT)
                .nominalTransaction(profit)
                .beginBalance(oldAccount.getPrincipalAmount())
                .endBalance(oldAccount.getPrincipalAmount())
                .description("Pembayaran keuntungan pada saat rollover/perpanjangan")
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

}
