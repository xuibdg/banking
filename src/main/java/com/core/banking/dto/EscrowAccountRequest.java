package com.core.banking.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EscrowAccountRequest {
    private String purpose;
    private String payerCustomerId;
    private String beneficiaryCustomerId;

}
