package com.core.banking.dto;

import com.core.banking.enums.EscrowAccountStatus;
import lombok.*;

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
    private String payerCustomerId;
    private String payerCustomerName;
    private String beneficiaryCustomerId;
    private String beneficiaryCustomerName;



}
