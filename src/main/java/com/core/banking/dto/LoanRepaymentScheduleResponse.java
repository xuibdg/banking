package com.core.banking.dto;

import com.core.banking.enums.LoanRepaymentStatus;
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
public class LoanRepaymentScheduleResponse {
    private String loanAccountId;
    private BigDecimal paymentAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal feePaid;
    private BigDecimal penaltyPaid;
    private String status;
    private String message;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal amountPaid;
    private LoanRepaymentStatus paymentStatus;
    private Timestamp paymentDate;
}
