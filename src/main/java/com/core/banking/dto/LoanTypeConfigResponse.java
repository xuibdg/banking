package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanTypeConfigResponse {
    private String loanTypeConfigId;
    private String loanTypeName;
    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private Integer minDurationMonths;
    private Integer maxDurationMonths;
    private BigDecimal interestRatePa;
    private String repaymentFrequency;
    private BigDecimal latePaymentFee;
    private String latePaymentFeeType;
    private Boolean isActive;
}
