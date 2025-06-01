package com.core.banking.dto;

import com.core.banking.enums.LoanTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransactionRequest {
    private String loanAccountId;
    private String loanRepaymentScheduleId;
    private LoanTransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal feeComponent;
    private Timestamp transactionDate;
    private String description;
    private String referenceNumber;
}
