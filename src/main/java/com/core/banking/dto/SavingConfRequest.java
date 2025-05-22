package com.core.banking.dto;

import com.core.banking.enums.SavingType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingConfRequest {

    @NotNull(message = "Saving type is required")
    private SavingType savingType;

    @NotNull(message = "Minimum limit is required")
    @DecimalMin(value = "0.0", message = "Minimum limit must be >= 0")
    private BigDecimal minLimit;

    @NotNull(message = "Maximum limit is required")
    @DecimalMin(value = "0.0", message = "Maximum limit must be >= 0")
    private BigDecimal maxLimit;

    @NotNull(message = "Daily transaction limit is required")
    @DecimalMin(value = "0.0", message = "Daily transaction limit must be >= 0")
    private BigDecimal limitTransactionDaily;

    @DecimalMin(value = "0.0", message = "Interest rate must be >= 0")
    @DecimalMax(value = "100.0", message = "Interest rate must be <= 100")
    private BigDecimal interestRate;

    @DecimalMin(value = "0.0", message = "Monthly fee must be >= 0")
    private BigDecimal monthlyFee;
}
