package com.core.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor

public class DepositAccountRequest {
    @NotNull(message = "Customer ID cannot be empty")
    private String customerId;

    @NotNull(message = "Deposit Type Config ID cannot be empty")
    private Long depositTypeConfigId;

    @NotNull(message = "Nominal Deposit cannot by empty")
    @DecimalMin(value = "0.01", message = "Nominal Deposit must be greater than zero")
    private BigDecimal nominalDeposit;

    // Possible values: PRINCIPAL_ONLY, PRINCIPAL_AND_PROFIT, NO_ROLLOVER
    @NotNull(message = "Rollover option is required")
    private String rolloverOption;
}
