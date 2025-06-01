package com.core.banking.dto;

import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.enums.Frequency;
import com.core.banking.enums.SavingTypeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingConfResponse {

    private String savingTypeConfigId;
    private BigDecimal minInitialDeposit;
    private BigDecimal minBalanceLimit;
    private BigDecimal maxBalanceLimit;
    private BigDecimal dailyTransactionLimit;
    private Integer dailyTransactionCountLimit;
    private BigDecimal interestRatePa;
    private Frequency interestCalculationFrequency;
    private Frequency interestPayoutFrequency;
    private BigDecimal monthlyMaintenanceFee;
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
