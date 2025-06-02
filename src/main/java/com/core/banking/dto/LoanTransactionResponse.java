package com.core.banking.dto;

import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.enums.LoanTransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanTransactionResponse {
    private String loanAccountId;
    private LoanAccountStatus status;
    private BigDecimal installmentAmount;
    private LocalDate firstRepaymentDate;
    private String message;
    private String loanTransactionId;
    private String loanRepaymentScheduleId;
    private LoanTransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal feeComponent;
    private Timestamp transactionDate;
    private String description;
    private String referenceNumber;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
