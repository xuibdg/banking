package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalReportDto {
    private String journalCode;
    private String referenceNumber;
    private LocalDate systemDate;
    private String coaCode;
    private String coaName;
    private BigDecimal debit;
    private BigDecimal credit;
    private String description;
    private LocalDateTime transactionDate;
}

