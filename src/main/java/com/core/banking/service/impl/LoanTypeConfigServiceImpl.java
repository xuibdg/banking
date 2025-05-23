package com.core.banking.service.impl;

import com.core.banking.dto.LoanTypeConfigRequest;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LoanTypeConfigServiceImpl implements LoanTypeConfigService {

    @Autowired
    private LoanTypeConfigRepository loanTypeConfigRepository;

    @Autowired
    private LoanTypeRepository loanTypeRepository;

    @Override
    public String createLoanTypeConfig(LoanTypeConfigRequest request) {
        LoanType loanType = loanTypeRepository.findById(request.getLoanTypeId())
                .orElseThrow(() -> new RuntimeException("Loan type tidak tersedia"));

        if (request.getMinLoanAmount().compareTo(request.getMaxLoanAmount()) > 0) {
            throw new RuntimeException("Min loan amount tidak boleh lebih besar dari max loan amount");
        }

        LoanTypeConfig typeConfig = new LoanTypeConfig();
        typeConfig.setLoanTypeConfigId(UUID.randomUUID().toString());  // Jangan lupa generate ID
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
        typeConfig.setCreatedAt(OffsetDateTime.now());

        loanTypeConfigRepository.save(typeConfig);

        return "Succes membuat loan type config";
    }

    @Override
    public List<LoanTypeConfig> findAll() {

        return loanTypeConfigRepository.findAll();
    }

    @Override
    public String updateLoanTypeConfig(String loanTypeConfigId, LoanTypeConfigRequest request) {
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
        typeConfig.setUpdatedAt(OffsetDateTime.now());

        loanTypeConfigRepository.save(typeConfig);
        return "update berhasil";
    }

    @Override
    public String deleteLoanTypeConfig(String loanTypeConfigId) {
        LoanTypeConfig typeConfig = loanTypeConfigRepository.findById(String.valueOf(loanTypeConfigId))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

//        typeConfig.setIsDeleted(true);
        loanTypeConfigRepository.save(typeConfig);
        return "";
    }


}
