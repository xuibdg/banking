package com.core.banking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalDetailRequest {
    private String coaId;
    private String mutationType;
    private BigDecimal amount;
    private String description;
}
