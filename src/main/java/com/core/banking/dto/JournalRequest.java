package com.core.banking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class JournalRequest {
    private String mutation;
    private String status;
    private String description;
    private String referenceNumber;
    private String referenceType;
    private List<JournalDetailRequest> details;
}
