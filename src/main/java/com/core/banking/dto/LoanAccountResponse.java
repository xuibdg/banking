package com.core.banking.dto;

import com.core.banking.enums.LoanAccountStatus;
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
public class LoanAccountResponse {
    private String loanAccountId;
    private String accountNumber;
    private LoanAccountStatus accountStatus;
    private Timestamp applicationDate;
    private Timestamp closedAt;
    private Timestamp createdAt;
    private LocalDate disburstmentDate;
    private int durationMonths;
    private LocalDate firstRepaymentdate;
    private BigDecimal installmentAmount;
    private BigDecimal intrestRateApplied;
    private LocalDate lastRepaymentDate;
    private BigDecimal outStandingPrincipal;
    private BigDecimal principalAmount;
    private Timestamp updatedAt;
    private String customerId;
    private String loanTypeConfigId;
    private Boolean isDeleted;
    }

