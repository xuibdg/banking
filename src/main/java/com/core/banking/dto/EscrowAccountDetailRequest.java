package com.core.banking.dto;

import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.TypeSenderBank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EscrowAccountDetailRequest {
    @NotEmpty
    private String escrowAccount;
    @NotEmpty
    private EscrowTransactionType transactionType;
    @NotEmpty
    private MutationType mutationType;
    @NonNull
    private BigDecimal nominalTransaction;
    @NotEmpty
    private String description;

    //need for Payment gateway
    @NotEmpty
    private String senderBank;

}
