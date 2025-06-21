package com.core.banking.dto;

import lombok.Data;

@Data
public class EscrowCreateToPGRequest {
    private EscrowAccountDetailRequest escrowAccountDetailRequest;
    private EscrowRequestToPGRequest pgRequest;
}
