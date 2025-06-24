package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositProfitSharingRequest {
    private Long depositAccountId;
    private LocalDate profitPeriodStartDate;
    private BigDecimal nominalProfitShared;
    private LocalDate profitPeriodEndDate;
}
