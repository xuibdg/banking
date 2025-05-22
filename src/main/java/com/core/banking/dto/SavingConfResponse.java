package com.core.banking.dto;

import com.core.banking.enums.SavingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingConfResponse {
    private Long id;
    private SavingType savingType;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private BigDecimal limitTransactionDaily;
    private BigDecimal interestRate;
    private BigDecimal monthlyFee;
    private Long version;
}
