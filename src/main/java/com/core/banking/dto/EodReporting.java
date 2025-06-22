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
public class EodReporting {
    private String coaCode;
    private String coaName;
    private String category;
    private BigDecimal amount;
}
