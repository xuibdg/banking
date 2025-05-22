package com.core.banking.service.impl;

import com.core.banking.dto.SavingConfRequest;
import com.core.banking.dto.SavingConfResponse;
import com.core.banking.entity.SavingConfiguration;
import com.core.banking.repository.SavingConfigurationRepository;
import com.core.banking.service.SavingTypeConfService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingConfServiceImpl implements SavingTypeConfService {
    private final SavingConfigurationRepository repository;

    @Override
    @Transactional
    public SavingConfResponse createOrUpdateConfiguration(SavingConfRequest request) {
        var existingConfig = repository.findBySavingType(request.getSavingType());

        SavingConfiguration config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setMinLimit(request.getMinLimit());
            config.setMaxLimit(request.getMaxLimit());
            config.setLimitTransactionDaily(request.getLimitTransactionDaily());
            config.setInterestRate(request.getInterestRate());
            config.setMonthlyFee(request.getMonthlyFee());
        } else {
            config = SavingConfiguration.builder()
                    .savingType(request.getSavingType())
                    .minLimit(request.getMinLimit())
                    .maxLimit(request.getMaxLimit())
                    .limitTransactionDaily(request.getLimitTransactionDaily())
                    .interestRate(request.getInterestRate())
                    .monthlyFee(request.getMonthlyFee())
                    .build();
        }

        SavingConfiguration savedConfig = repository.save(config);

        return mapToResponse(savedConfig);
    }

    private SavingConfResponse mapToResponse(SavingConfiguration config) {
        return SavingConfResponse.builder()
                .id(config.getId())
                .savingType(config.getSavingType())
                .minLimit(config.getMinLimit())
                .maxLimit(config.getMaxLimit())
                .limitTransactionDaily(config.getLimitTransactionDaily())
                .interestRate(config.getInterestRate())
                .monthlyFee(config.getMonthlyFee())
                .version(config.getVersion())
                .build();
    }
    @Override
    public List<SavingConfResponse> getAllConfigurations() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Override
    public SavingConfResponse getConfigurationById(Long id) {
        SavingConfiguration config = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration not found with id: " + id));
        return mapToResponse(config);
    }
    @Override
    @Transactional
    public SavingConfResponse updateConfiguration(Long id, SavingConfRequest request) {
        SavingConfiguration existingConfig = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration not found with id: " + id));

        existingConfig.setSavingType(request.getSavingType());
        existingConfig.setMinLimit(request.getMinLimit());
        existingConfig.setMaxLimit(request.getMaxLimit());
        existingConfig.setLimitTransactionDaily(request.getLimitTransactionDaily());
        existingConfig.setInterestRate(request.getInterestRate());
        existingConfig.setMonthlyFee(request.getMonthlyFee());

        SavingConfiguration updatedConfig = repository.save(existingConfig);
        return mapToResponse(updatedConfig);
    }
}
