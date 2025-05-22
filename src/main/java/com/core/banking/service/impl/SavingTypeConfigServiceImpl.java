package com.core.banking.service.impl;


import com.core.banking.dto.SavingTypeRequest;
import com.core.banking.dto.SavingTypeResponse;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.repository.SavingConfigurationRepository;
import com.core.banking.repository.SavingTypeConfigRepository;
import com.core.banking.service.SavingTypeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingTypeConfigServiceImpl implements SavingTypeConfigService {

    @Autowired
    private SavingTypeConfigRepository savingTypeConfigRepository;

    @Override
    public List<SavingTypeConfig> findAll() {
        return savingTypeConfigRepository.findAll();
    }

    private final SavingConfigurationRepository repository;


    @Override
    public SavingTypeResponse createOrUpdateConfiguration(SavingTypeRequest request) {

        Optional<SavingTypeConfig> existingConfig = this.savingTypeConfigRepository.findBySavingType(request.getSavingType());
        SavingTypeConfig config;
        if (existingConfig.isPresent()) {
            config = (SavingTypeConfig)existingConfig.get();
            config.setSavingType(request.getSavingType());
            config.setMinBalanceLimit(request.getMinBalanceLimit());
            config.setMaxBalanceLimit(request.getMaxBalanceLimit());
            config.setDailyTransactionLimit(request.getDailyTransactionLimit());
            config.setDailyTransactionCountLimit(request.getDailyTransactionCountLimit());
            config.setInterestRatePa(request.getInterestRatePa());
            config.setInterestCalculationFrequency(request.getInterestCalculationFrequency());
            config.setInterestPayoutFrequency(request.getInterestPayoutFrequency());
            config.setMonthlyMaintenanceFee(request.getMonthlyMaintenanceFee());
            config.setIsActive(request.getIsActive());
        } else {
            config = SavingTypeConfig.builder().savingType(request.getSavingType()).minInitialDeposit(request.getMinInitialDeposit()).minBalanceLimit(request.getMinBalanceLimit()).maxBalanceLimit(request.getMaxBalanceLimit()).dailyTransactionLimit(request.getDailyTransactionLimit()).dailyTransactionCountLimit(request.getDailyTransactionCountLimit()).interestRatePa(request.getInterestRatePa()).interestCalculationFrequency(request.getInterestCalculationFrequency()).interestPayoutFrequency(request.getInterestPayoutFrequency()).monthlyMaintenanceFee(request.getMonthlyMaintenanceFee()).isActive(request.getIsActive()).build();
        }

        SavingTypeConfig savedConfig = (SavingTypeConfig)this.savingTypeConfigRepository.save(config);
        return this.mapToResponse(savedConfig);
    }

    public SavingTypeResponse mapToResponse(SavingTypeConfig config) {
        return SavingTypeResponse.builder().savingType(config.getSavingType()).minInitialDeposit(config.getMinInitialDeposit()).minBalanceLimit(config.getMinBalanceLimit()).maxBalanceLimit(config.getMaxBalanceLimit()).dailyTransactionLimit(config.getDailyTransactionLimit()).dailyTransactionCountLimit(config.getDailyTransactionCountLimit()).interestRatePa(config.getInterestRatePa()).interestCalculationFrequency(config.getInterestCalculationFrequency()).interestPayoutFrequency(config.getInterestPayoutFrequency()).monthlyMaintenanceFee(config.getMonthlyMaintenanceFee()).isActive(config.getIsActive()).createdAt(config.getCreatedAt()).updateAt(config.getUpdatedAt()).build();
    }

    @Override
    public List<SavingTypeResponse> getAllConfigurations() {
        return (List)this.savingTypeConfigRepository.findAll()
                .stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public SavingTypeResponse getConfigurationById(Long id) {
        SavingTypeConfig config = (SavingTypeConfig)this.savingTypeConfigRepository.findById(id.toString()).orElseThrow(() -> new RuntimeException("Configuration not found with id: " + id));
        return this.mapToResponse(config);
    }

    @Override
    public SavingTypeResponse updateConfiguration(Long id, SavingTypeRequest request) {
        SavingTypeConfig existingConfig = (SavingTypeConfig)this.savingTypeConfigRepository.findById(id.toString()).orElseThrow(() -> new RuntimeException("Configuration not found with id: " + id));
        existingConfig.setSavingType(request.getSavingType());
        existingConfig.setMinInitialDeposit(request.getMinInitialDeposit());
        existingConfig.setMinBalanceLimit(request.getMinBalanceLimit());
        existingConfig.setMaxBalanceLimit(request.getMaxBalanceLimit());
        existingConfig.setDailyTransactionLimit(request.getDailyTransactionLimit());
        existingConfig.setDailyTransactionCountLimit(request.getDailyTransactionCountLimit());
        existingConfig.setInterestRatePa(request.getInterestRatePa());
        existingConfig.setInterestCalculationFrequency(request.getInterestCalculationFrequency());
        existingConfig.setInterestPayoutFrequency(request.getInterestPayoutFrequency());
        existingConfig.setMonthlyMaintenanceFee(request.getMonthlyMaintenanceFee());
        existingConfig.setIsActive(request.getIsActive());
        SavingTypeConfig updatedConfig = (SavingTypeConfig)this.savingTypeConfigRepository.save(existingConfig);
        return this.mapToResponse(updatedConfig);
    }


}
