package com.core.banking.dto;

import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.TransactionTypeStatus;
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
public class EscrowAccountResponse {
    private String id;
    private String accountNumber;
    private String purpose;
    private BigDecimal currentBalance;
    private EscrowAccountStatus accountStatus;
    private String payerCustomer;
    private String payerCustomerName;
    private String beneficiaryCustomer;
    private String beneficiaryCustomerName;
    private String savingAccount;
    private String loanAccount;
    private String depositAccount;
    private TransactionTypeStatus transactionType;
    private String releaseAccountNumber;
    private BigDecimal nominalTransaction;




}
