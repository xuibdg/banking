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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public List<DepositProfitSharingResponse> createCalculateDepositSharing(DepositProfitSharingRequest request, UserMetaData userMetaData) {
        LocalDate start = request.getProfitPeriodStartDate();
        LocalDate end = request.getProfitPeriodEndDate();

        if (start == null || end == null || start.isAfter(end)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_DATE_RANGE);
        }

        List<DepositAccount> accounts;

        if (request.getDepositAccountId() != null) {
            DepositAccount account = depositAccountRepository.findById(request.getDepositAccountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));

            if (account.getAccountStatus() != DepositAccountStatus.ACTIVE) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_ACTIVE);
            }

            accounts = List.of(account);
        } else {
            accounts = depositAccountRepository.findByAccountStatus(DepositAccountStatus.ACTIVE);
        }

        List<DepositProfitSharingResponse> result = new ArrayList<>();
        LocalDate current = start.withDayOfMonth(1);

        while (!current.isAfter(end)) {
            LocalDate monthStart = current;
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            if (monthEnd.isAfter(end)) {
                monthEnd = end;
            }

            for (DepositAccount account : accounts) {
                LocalDate openedAt = account.getOpenedAt().toLocalDate();
                LocalDate maturityDate = account.getMaturityDate();

                if (monthStart.isBefore(openedAt) || monthEnd.isAfter(maturityDate)) {
                    continue;
                }

                boolean alreadyExists = depositProfitSharingRepository.existsByDepositAccountAndProfitPeriodStartDateAndProfitPeriodEndDate(
                        account, monthStart, monthEnd
                );
                if (alreadyExists) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.PROFIT_SHRING_ALREADY_EXIST);
                }


                BigDecimal principal = account.getPrincipalAmount();
                BigDecimal percenPa = account.getDepositTypeConfig().getProfitSharePercentagePa();
                BigDecimal monthlyProfit = principal
                        .multiply(percenPa)
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

                DepositProfitSharing entity = new DepositProfitSharing();
                entity.setDepositAccount(account);
                entity.setProfitPeriodStartDate(monthStart);
                entity.setProfitPeriodEndDate(monthEnd);
                entity.setNominalProfitShared(monthlyProfit);
                entity.setPayoutDate(LocalDateTime.now());
                entity.setCreatedAt(LocalDateTime.now());

                depositProfitSharingRepository.save(entity);

                DepositProfitSharingResponse response = new DepositProfitSharingResponse();
                response.setDepositAccountId(account.getDepositoAccountId());
                response.setProfitPeriodStartDate(monthStart);
                response.setProfitPeriodEndDate(monthEnd);
                response.setNominalProfitShared(monthlyProfit);
                response.setPayoutDate(entity.getPayoutDate());
                response.setCreatedAt(entity.getCreatedAt());

                result.add(response);
            }

            current = current.plusMonths(1);
        }

        return result;
    }


    @Override
    public String updateDepositProfitSharing(Long id, DepositProfitSharingRequest request, UserMetaData userMetaData) {

        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND);
        }
        if (request.getProfitPeriodStartDate() != null && request.getProfitPeriodEndDate() != null) {
            if (request.getProfitPeriodStartDate().isAfter(request.getProfitPeriodEndDate())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_DATE_RANGE);
            }
        }

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
        if (request.getNominalProfitShared() != null) {
            entity.setNominalProfitShared(request.getNominalProfitShared());
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

    @Override
    public List<DepositProfitSharingResponse> findById(Long depositAcountId) {
        List<DepositProfitSharing> entities = depositProfitSharingRepository
                .findByDepositAccount_DepositoAccountId(depositAcountId);

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
        response.setPayoutDate(entity.getPayoutDate());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

}
