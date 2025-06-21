package com.core.banking.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PGTransactionFromEscrowRequest {
    private String escrowAccountId;
    private BigDecimal amount;

}
