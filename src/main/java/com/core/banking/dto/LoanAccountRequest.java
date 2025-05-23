package com.core.banking.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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
}
