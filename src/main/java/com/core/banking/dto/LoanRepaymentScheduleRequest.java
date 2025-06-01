package com.core.banking.dto;

import com.core.banking.enums.LoanRepaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentScheduleRequest {
    private String loanAccountId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal totalDue;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal amountPaid;
    private Timestamp paymentDate;
    private LoanRepaymentStatus paymentStatus;
    private String loanRepaymentScheduleId;
}

