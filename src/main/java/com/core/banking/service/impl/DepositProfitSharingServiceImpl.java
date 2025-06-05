package com.core.banking.service.impl;

import com.core.banking.dto.DepositProfitSharingRequest;
import com.core.banking.dto.DepositProfitSharingResponse;
import com.core.banking.dto.UserMetaData;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public DepositProfitSharingResponse createProcessDepositSharing(DepositProfitSharingRequest depositProfitSharingRequest, UserMetaData userMetaData) {
        List<DepositAccount> accounts = depositAccountRepository.findByAccountStatus(DepositAccountStatus.ACTIVE);
        if (accounts.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND);
        }
        BigDecimal totalProfitBank = totalProfitBank();
        BigDecimal totalDailyBalance = BigDecimal.ZERO;
        Map<Long, BigDecimal> accountDailyBalances = new HashMap<>();

        for (DepositAccount account : accounts) {
            LocalDateTime start = account.getOpenedAt();
            LocalDateTime end = account.getMaturityDate().atStartOfDay();
            long daysActive = Duration.between(start, end).toDays();
            if (daysActive <= 0) continue;
            BigDecimal dailyBalance = account.getPrincipalAmount().multiply(BigDecimal.valueOf(daysActive));
            accountDailyBalances.put(account.getDepositoAccountId(), dailyBalance);
            totalDailyBalance = totalDailyBalance.add(dailyBalance);
        }
        DepositProfitSharing lastSharing = null;
        for (DepositAccount account : accounts) {
            BigDecimal accountDailyBalance = accountDailyBalances.get(account.getDepositoAccountId());
            if (accountDailyBalance == null || accountDailyBalance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal nasabahRatio = account.getDepositTypeConfig().getProfitSharingRatioCustomer();
            if (nasabahRatio == null) nasabahRatio = BigDecimal.ZERO;

            BigDecimal portion = accountDailyBalance.divide(totalDailyBalance, 10, RoundingMode.HALF_UP);
            BigDecimal profitShared = totalProfitBank.multiply(portion).multiply(nasabahRatio).setScale(2, RoundingMode.HALF_UP);

            DepositProfitSharing sharing = new DepositProfitSharing();
            sharing.setDepositAccount(account);
            sharing.setProfitPeriodStartDate(account.getOpenedAt().toLocalDate());
            sharing.setProfitPeriodEndDate(account.getMaturityDate());
            sharing.setNominalProfitShared(profitShared);
            sharing.setPayoutDate(LocalDateTime.now());
            sharing.setCreatedAt(LocalDateTime.now());

            lastSharing = depositProfitSharingRepository.save(sharing);
        }

        if (lastSharing == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NO_PRROFIT_SHARING_PROCESSED);
        }
        return mapToResponse(lastSharing);
    }

    @Override
    public BigDecimal totalProfitBank() {
        return new BigDecimal("10000000.00");
    }

    @Override
    public String updateDepositProfitSharing(Long id, DepositProfitSharingRequest request, UserMetaData userMetaData) {
        DepositProfitSharing entity = depositProfitSharingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_DEPOSIT_SHARING_NOT_FOUND));
        if (request.getDepositAccountId() != null) {
            DepositAccount account = depositAccountRepository.findById(request.getDepositAccountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
            entity.setDepositAccount(account);
        }
        if (request.getProfitPeriodStartDate() != null) {
            entity.setProfitPeriodStartDate(request.getProfitPeriodStartDate());
        }
        if (request.getProfitPeriodEndDate() != null) {
            entity.setProfitPeriodEndDate(request.getProfitPeriodEndDate());
        }
        entity.setPayoutDate(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        depositProfitSharingRepository.save(entity);
        return "Deposit Profit Sharing updated successfully";
    }

    @Override
    public String deleteDepositProfitSharing(Long id, UserMetaData userMetaData) {
        DepositProfitSharing entity = depositProfitSharingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_DEPOSIT_SHARING_NOT_FOUND));
        depositProfitSharingRepository.delete(entity);
        return "Deposit Profit Sharing deleted successfully";
    }


    @Override
    public List<DepositProfitSharingResponse> findAll() {
        List<DepositProfitSharing> entities = depositProfitSharingRepository.findAll();
        List<DepositProfitSharingResponse> responses = new ArrayList<>();
        for (DepositProfitSharing entity : entities) {
            responses.add(mapToResponse(entity));
        }
        return responses;
    }

    private DepositProfitSharingResponse mapToResponse(DepositProfitSharing entity) {
        DepositProfitSharingResponse response = new DepositProfitSharingResponse();
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