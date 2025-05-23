package com.core.banking.dto;

import com.core.banking.enums.LoanTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransactionRequest {
    private String loanAccountId;
    private String loanRepaymentScheduleId; // Optional / nullable
    private LoanTransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal feeComponent;
    private OffsetDateTime transactionDate;
    private String description;
    private String referenceNumber;
}
