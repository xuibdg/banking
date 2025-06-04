package com.core.banking.service.impl;

import com.core.banking.dto.DepositProfitSharingRequest;
import com.core.banking.dto.DepositProfitSharingResponse;
import com.core.banking.entity.DepositAccount;
import com.core.banking.entity.DepositProfitSharing;
import com.core.banking.enums.DepositAccountStatus;
import com.core.banking.repository.DepositAccountDetailRepository;
import com.core.banking.repository.DepositAccountRepository;
import com.core.banking.repository.DepositProfitSharingRepository;
import com.core.banking.service.DepositProfitSharingService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepositProfitSharingServiceImpl implements DepositProfitSharingService {

    @Autowired
    private DepositProfitSharingRepository depositProfitSharingRepository;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private DepositAccountDetailRepository depositAccountDetailRepository;

    @Override
    @Transactional
    public void processProfitSharing(LocalDate profitPeriodStart, LocalDate profitPeriodEnd) {
        List<DepositAccount> accounts = depositAccountRepository.findByAccountStatus(DepositAccountStatus.ACTIVE);
        if (accounts.isEmpty()) return;

        BigDecimal totalProfitBank = totalProfitBank();

        Map<Long, BigDecimal> accountDailyBalances = new HashMap<>();
        BigDecimal totalDailyBalance = BigDecimal.ZERO;

        for (DepositAccount account : accounts) {
            LocalDate accountStart = account.getOpenedAt().toLocalDate();
            LocalDate accountEnd = account.getMaturityDate();

            LocalDate start = accountStart.isAfter(profitPeriodStart) ? accountStart : profitPeriodStart;
            LocalDate end = accountEnd.isBefore(profitPeriodEnd) ? accountEnd : profitPeriodEnd;

            if (start.isAfter(end)) continue;

            long daysActive = ChronoUnit.DAYS.between(start, end.plusDays(1));
            if (daysActive <= 0) continue;

            BigDecimal dailyBalance = account.getPrincipalAmount().multiply(BigDecimal.valueOf(daysActive));

            accountDailyBalances.put(account.getDepositoAccountId(), dailyBalance);
            totalDailyBalance = totalDailyBalance.add(dailyBalance);
        }

        for (DepositAccount account : accounts) {
            Long accountId = account.getDepositoAccountId();
            BigDecimal accountDailyBalance = accountDailyBalances.get(account.getDepositoAccountId());
            if (accountDailyBalance == null || accountDailyBalance.compareTo(BigDecimal.ZERO) == 0) continue;

            boolean alreadyShared = depositProfitSharingRepository.existsByDepositAccountAndProfitPeriodStartDateAndProfitPeriodEndDate(account, profitPeriodStart, profitPeriodEnd);
            BigDecimal nasabahRatio = account.getDepositTypeConfig().getProfitSharingRatioCustomer();
            if (nasabahRatio == null) nasabahRatio = BigDecimal.ZERO;

            BigDecimal portion = accountDailyBalance.divide(totalDailyBalance, 10, RoundingMode.HALF_UP);
            BigDecimal profitShare = totalProfitBank.multiply(portion).multiply(nasabahRatio).setScale(2, RoundingMode.HALF_UP);

            DepositProfitSharing sharing = new DepositProfitSharing();
            sharing.setDepositAccount(account);
            sharing.setProfitPeriodStartDate(account.getOpenedAt().toLocalDate());
            sharing.setProfitPeriodEndDate(account.getMaturityDate());
            sharing.setNominalProfitShared(profitShare);
            sharing.setPayoutDate(LocalDateTime.now());
            sharing.setCreatedAt(LocalDateTime.now());

            depositProfitSharingRepository.save(sharing);
        }
    }

    @Override
    public BigDecimal totalProfitBank() {
        return new BigDecimal("10000000.00");
    }

    @Override
    public DepositProfitSharingResponse update(String id, DepositProfitSharingRequest request) {
        Long depositProfitSharingId;
        try {
            depositProfitSharingId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid ID format: " + id);
        }
        DepositProfitSharing entity = depositProfitSharingRepository.findById(depositProfitSharingId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_DEPOSIT_SHARING_NOT_FOUND));
        entity.setProfitPeriodStartDate(request.getProfitPeriodStartDate());
        entity.setProfitPeriodEndDate(request.getProfitPeriodEndDate());
        entity.setCreatedAt(LocalDateTime.now());
        if (request.getDepositAccountId() != null) {
            DepositAccount depositAccount = depositAccountRepository.findById(request.getDepositAccountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
            entity.setDepositAccount(depositAccount);
        }

        depositProfitSharingRepository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public void delete(String id) {
        Long depositProfitSharingId;
        try {
            depositProfitSharingId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid ID format: " + id);
        }
        DepositProfitSharing entity = depositProfitSharingRepository.findById(depositProfitSharingId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_DEPOSIT_SHARING_NOT_FOUND));
        depositProfitSharingRepository.delete(entity);
    }

    @Override
    public List<DepositProfitSharing> findAll() {
        return depositProfitSharingRepository.findAll();
    }

    private DepositProfitSharingResponse mapToResponse(DepositProfitSharing entity) {
        DepositProfitSharingResponse response = new DepositProfitSharingResponse();
        response.setDepositoProfitSharingId(entity.getDepositoProfitSharingId());
        response.setDepositAccountId(entity.getDepositAccount() != null ? entity.getDepositAccount().getDepositoAccountId() : null);
        response.setProfitPeriodStartDate(entity.getProfitPeriodStartDate());
        response.setProfitPeriodEndDate(entity.getProfitPeriodEndDate());
        response.setNominalProfitShared(entity.getNominalProfitShared());
        response.setTotalProfitBank(new BigDecimal("10000000.00"));
        response.setPayoutDate(entity.getPayoutDate());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}