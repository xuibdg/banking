package com.core.banking.dto;

import com.core.banking.enums.TransactionTypeStatus;
import jakarta.validation.constraints.NotEmpty;
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
public class EscrowAccountRequest {
    private String purpose;
    private String payerCustomer;
    private String beneficiaryCustomer;
    private String savingAccount;
    private String loanAccount;
    private Long depositAccount;
    private TransactionTypeStatus transactionTypeStatus;

    @NotEmpty
    private BigDecimal nominalTransaction;

    @NotEmpty
    private String releaseAccountNumber;

    // Tambahan untuk keperluan Payment Gateway
    private String senderBank;
    private String paymentGatewayId;
    private String description;

}
