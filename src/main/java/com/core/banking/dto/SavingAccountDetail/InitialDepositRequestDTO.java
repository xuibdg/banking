package com.core.banking.dto.SavingAccountDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitialDepositRequestDTO {

    private String savingAccountNumber;
    private BigDecimal amount;
    private String description;
    private String sourceEscrowAccountNumber;
    private String channel;
}