package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositProfitSharingResponse {
    private Long depositoProfitSharingId;
    private Long depositAccountId;
    private Long depositAccountDetailId;
    private LocalDate profitPeriodStartDate;
    private LocalDate profitPeriodEndDate;
    private BigDecimal nominalProfitShared;
    private BigDecimal totalProfitBank;
    private LocalDateTime payoutDate;
    private LocalDateTime createdAt;
}
