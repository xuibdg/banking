package com.core.banking.dto;

import com.core.banking.enums.LoanFeeType;
import com.core.banking.enums.Frequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTypeConfigRequest {
    private String loanTypeId;
    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private Integer minDurationMonths;
    private Integer maxDurationMonths;
    private BigDecimal interestRatePa;
    private Frequency repaymentFrequency;
    private BigDecimal latePaymentFee;
    private LoanFeeType latePaymentFeeType;
    private Boolean isActive;
}
