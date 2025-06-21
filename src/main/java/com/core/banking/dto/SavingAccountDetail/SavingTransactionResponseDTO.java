package com.core.banking.dto.SavingAccountDetail;


import com.core.banking.enums.MutationType;
import com.core.banking.enums.SavingTransactionType;
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
public class SavingTransactionResponseDTO {
    private String transactionId;
    private String savingAccountNumber;
    private SavingTransactionType transactionType;
    private MutationType mutationType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String transactionReference;
    private String channel;
    private Timestamp transactionTimestamp;
    private Timestamp createdAt;
}