package com.core.banking.dto;

import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.TransactionTypeStatus;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

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
