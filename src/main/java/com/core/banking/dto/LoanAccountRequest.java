package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccountRequest {
    private String customerId;
    private String loanTypeConfigId;
    private String accountNumber;
    private BigDecimal principalAmount;
    private BigDecimal interestRateApplied;
    private Integer durationMonths;
    private BigDecimal outstandingPrincipal;
    private BigDecimal installmentAmount;
    private LocalDate disbursementDate;
    private LocalDate firstRepaymentDate;
    private LocalDate lastRepaymentDate;
    private OffsetDateTime applicationDate;
    private Boolean isDeleted;
}
