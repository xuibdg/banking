package com.core.banking.dto;

import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.enums.MutationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EscrowAccountDetailResponse {
    private String id;
    private String escrowAccount;
    private EscrowAccountStatus escrowAccountStatus;
    private EscrowTransactionType transactionType;
    private MutationType mutationType;
    private BigDecimal nominalTransaction;
    private BigDecimal beginBalance;
    private BigDecimal endBalance;
    private String description;
    private String transactionReference;
    private String releaseAccountNumber;
}
