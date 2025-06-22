package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class JournalResponse {
    private String journalId;
    private String journalCode;
    private String referenceNumber;
    private String status;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private String description;
    private Boolean isPosted;
    private LocalDate systemDate;
    private List<JournalDetailResponse> details;
    private LocalDateTime createdAt;
    private String createdBy;
    private String message;
}
