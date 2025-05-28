package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTypeConfigRequest;
import com.core.banking.dto.LoanTypeConfigResponse;
import com.core.banking.entity.LoanType;
import com.core.banking.entity.LoanTypeConfig;
import com.core.banking.repository.LoanTypeConfigRepository;
import com.core.banking.repository.LoanTypeRepository;
import com.core.banking.service.LoanTypeConfigService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoanTypeConfigServiceImpl implements LoanTypeConfigService {

    @Autowired
    private LoanTypeConfigRepository loanTypeConfigRepository;

    @Autowired
    private LoanTypeRepository loanTypeRepository;

    @Override
    public String createLoanTypeConfig(LoanTypeConfigRequest request, UserMetaData userMetaData) {
        LoanType loanType = loanTypeRepository.findById(request.getLoanTypeId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (request.getMinLoanAmount().compareTo(request.getMaxLoanAmount()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.AMOUNT_NOT_ENOUGH);
        }

        LoanTypeConfig typeConfig = new LoanTypeConfig();
        typeConfig.setLoanTypeConfigId(UUID.randomUUID().toString());
        typeConfig.setLoanType(loanType);
        typeConfig.setMinLoanAmount(request.getMinLoanAmount());
        typeConfig.setMaxLoanAmount(request.getMaxLoanAmount());
        typeConfig.setMinDurationMonths(request.getMinDurationMonths());
        typeConfig.setMaxDurationMonths(request.getMaxDurationMonths());
        typeConfig.setInterestRatePa(request.getInterestRatePa());
        typeConfig.setRepaymentFrequency(request.getRepaymentFrequency());
        typeConfig.setLatePaymentFee(request.getLatePaymentFee());
        typeConfig.setLatePaymentFeeType(request.getLatePaymentFeeType());
        typeConfig.setIsActive(true);
        typeConfig.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanTypeConfigRepository.save(typeConfig);

        return "Succes membuat loan type config";
    }

    @Override
    public List<LoanTypeConfigResponse> findAll() {

        List<LoanTypeConfig> configs = loanTypeConfigRepository.findAll();

        return configs.stream()
                .map(config -> new LoanTypeConfigResponse(
                        config.getLoanTypeConfigId(),
                        config.getLoanType().getTypeName(),
                        config.getMinLoanAmount(),
                        config.getMaxLoanAmount(),
                        config.getMinDurationMonths(),
                        config.getMaxDurationMonths(),
                        config.getInterestRatePa(),
                        config.getRepaymentFrequency().name(),
                        config.getLatePaymentFee(),
                        config.getLatePaymentFeeType() != null ? config.getLatePaymentFeeType().name() : null,
                        config.getIsActive()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public String updateLoanTypeConfig(String loanTypeConfigId, LoanTypeConfigRequest request, UserMetaData userMetaData) {
        LoanTypeConfig typeConfig = loanTypeConfigRepository.findById(String.valueOf(loanTypeConfigId))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanType loanType = loanTypeRepository.findById(String.valueOf(request.getLoanTypeId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        typeConfig.setLoanType(loanType);
        typeConfig.setMinLoanAmount(request.getMinLoanAmount());
        typeConfig.setMaxLoanAmount(request.getMaxLoanAmount());
        typeConfig.setMinDurationMonths(request.getMinDurationMonths());
        typeConfig.setMaxDurationMonths(request.getMaxDurationMonths());
        typeConfig.setInterestRatePa(request.getInterestRatePa());
        typeConfig.setRepaymentFrequency(request.getRepaymentFrequency());
        typeConfig.setLatePaymentFee(request.getLatePaymentFee());
        typeConfig.setLatePaymentFeeType(request.getLatePaymentFeeType());
        typeConfig.setIsActive(request.getIsActive());
        typeConfig.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanTypeConfigRepository.save(typeConfig);
        return "update berhasil";
    }

    @Override
    public String deleteLoanTypeConfig(String loanTypeConfigId, UserMetaData userMetaData) {
        LoanTypeConfig typeConfig = loanTypeConfigRepository.findById(String.valueOf(loanTypeConfigId))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        typeConfig.setIsDeleted(true);
        loanTypeConfigRepository.save(typeConfig);
        return "Sukses delete loan type";
    }
}
