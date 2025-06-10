package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class EarlyWithdrawalResponse {
    private Long depositAccountId;
    private String accountNumber;
    private BigDecimal principalAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal penaltyPercentage;
    private BigDecimal returnedAmount;
    private LocalDate withdrawalDate;
}
