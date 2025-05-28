package com.core.banking.dto.SavingAccountDetail;

import com.core.banking.enums.MutationType;
import com.core.banking.enums.SavingTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositResponseDto {
    private String savingAccountDetailId;
    private String savingAccountId;
    private SavingTransactionType transactionType;
    private MutationType mutationType;
    private BigDecimal nominalTransaction;
    private BigDecimal beginBalance;
    private BigDecimal endBalance;
    private String description;
    private String transactionReference;
    private String channel;
    private Timestamp transactionAt;
    private Timestamp createdAt;
    private String statusMessage;
}
