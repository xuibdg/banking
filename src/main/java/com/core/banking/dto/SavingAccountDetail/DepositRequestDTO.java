package com.core.banking.dto.SavingAccountDetail;

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
public class DepositRequestDTO {

    private String savingAccountNumber;
    private BigDecimal amount;
    private String description;
    private String channel;

    @NotNull(message = "Payer customer ID cannot be null")
    private String payerCustomerId;
}