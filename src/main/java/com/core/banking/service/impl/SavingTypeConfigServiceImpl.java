package com.core.banking.service.impl;


import com.core.banking.dto.SavingTypeConfRequest;
import com.core.banking.entity.SavingType;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.repository.SavingTypeConfigRepository;
import com.core.banking.repository.SavingTypeRepository;
import com.core.banking.service.SavingTypeConfigService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SavingTypeConfigServiceImpl implements SavingTypeConfigService {

    @Autowired
    private SavingTypeConfigRepository savingTypeConfigRepository;
    @Autowired
    private SavingTypeRepository savingTypeRepository;

    @Transactional
    public SavingTypeConfig createSavingTypeConfig(SavingTypeConfRequest config) {
        validateConfig(config);

        SavingType savingType = savingTypeRepository.findById(config.getSavingTypeId())
                .orElseThrow(() -> new BusinessException(GlobalErrorMapping.DATA_NOT_FOUND+""));

        config.setSavingTypeId(savingType.getSavingTypeId());
        config.setCreatedAt(Timestamp.from(Instant.now()));
        config.setUpdatedAt(Timestamp.from(Instant.now()));

        return savingTypeConfigRepository.save(SavingTypeConfig.builder()
                        .savingType(savingType)
                        .minInitialDeposit(config.getMinInitialDeposit())
                        .minBalanceLimit(config.getMinBalanceLimit())
                        .maxBalanceLimit(config.getMaxBalanceLimit())
                        .dailyTransactionLimit(config.getDailyTransactionLimit())
                        .dailyTransactionCountLimit(config.getDailyTransactionCountLimit())
                        .interestRatePa(config.getInterestRatePa())
                        .interestCalculationFrequency(config.getInterestCalculationFrequency())
                        .interestPayoutFrequency(config.getInterestPayoutFrequency())
                        .monthlyMaintenanceFee(config.getMonthlyMaintenanceFee())
                        .isActive(config.getIsActive())
                        .createdAt(config.getCreatedAt())
                        .updatedAt(config.getUpdatedAt())
                        .build());
    }

    @Override
    public List<SavingTypeConfig> getAllConfigs() {
        return savingTypeConfigRepository.findAll();
    }

    @Override
    public Optional<SavingTypeConfig> getConfigById(String id) {
        return savingTypeConfigRepository.findById(id);
    }

    @Override
    public List<SavingTypeConfig> getConfigsBySavingTypeId(String savingTypeId) {
        return savingTypeConfigRepository.findBySavingTypeSavingTypeId(savingTypeId);
    }

    @Override
    public List<SavingTypeConfig> getActiveConfigs() {
        return savingTypeConfigRepository.findByIsActive(true);
    }

    @Override
    public SavingTypeConfig updateSavingTypeConfig(String id, SavingTypeConfRequest updatedConfig) {
        SavingTypeConfig existing = savingTypeConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GlobalErrorMapping.NOT_FOUND_ID +" ( "+ id+" ) "));

        existing.setMinInitialDeposit(updatedConfig.getMinInitialDeposit());
        existing.setMinBalanceLimit(updatedConfig.getMinBalanceLimit());
        existing.setMaxBalanceLimit(updatedConfig.getMaxBalanceLimit());
        existing.setDailyTransactionLimit(updatedConfig.getDailyTransactionLimit());
        existing.setDailyTransactionCountLimit(updatedConfig.getDailyTransactionCountLimit());
        existing.setInterestRatePa(updatedConfig.getInterestRatePa());
        existing.setInterestCalculationFrequency(updatedConfig.getInterestCalculationFrequency());
        existing.setInterestPayoutFrequency(updatedConfig.getInterestPayoutFrequency());
        existing.setMonthlyMaintenanceFee(updatedConfig.getMonthlyMaintenanceFee());
        existing.setIsActive(updatedConfig.getIsActive());
        existing.setUpdatedAt(Timestamp.from(Instant.now()));

        return savingTypeConfigRepository.save(existing);

    }

    private void validateConfig(SavingTypeConfRequest config) {
        if (config.getMinInitialDeposit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Min initial deposit cannot be negative");
        }

        if (config.getMinBalanceLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Min balance limit cannot be negative");
        }

        if (config.getMaxBalanceLimit() != null &&
                config.getMaxBalanceLimit().compareTo(config.getMinBalanceLimit()) < 0) {
            throw new BusinessException("Max balance limit cannot be less than min balance limit");
        }

        if (config.getDailyTransactionLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Daily transaction limit cannot be negative");
        }

        if (config.getDailyTransactionCountLimit() != null &&
                config.getDailyTransactionCountLimit() < 0) {
            throw new BusinessException("Daily transaction count limit cannot be negative");
        }

        if (config.getInterestRatePa().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Interest rate cannot be negative");
        }

        if (config.getMonthlyMaintenanceFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Monthly maintenance fee cannot be negative");
        }
    }
    @Override
    public String deleteSavingType(String id) {
        savingTypeConfigRepository.findById(id).map(data -> {
            data.setIsDeleted(Boolean.TRUE);
            savingTypeConfigRepository.save(data);
            return data;
        });
        return "SUCCESS DELETED";
    }


}
