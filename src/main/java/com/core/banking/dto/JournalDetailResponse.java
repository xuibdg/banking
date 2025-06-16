package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class JournalDetailResponse {
    private String coaCode;
    private String mutationType;
    private BigDecimal debit;
    private BigDecimal credit;
    private String description;
}
