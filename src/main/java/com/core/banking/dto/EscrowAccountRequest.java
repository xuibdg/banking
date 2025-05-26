package com.core.banking.dto;

import com.core.banking.enums.TransactionTypeStatus;
import lombok.*;

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
    private String depositAccount;
    private TransactionTypeStatus transactionTypeStatus;

}
