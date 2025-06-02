package com.core.banking.dto;

import com.core.banking.entity.DepositAccount;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositAccountResponse {
    private String customerName;
    private String depositTypeName;
    private BigDecimal profitSharePercentage;
    private Integer termInMonths;

    @JsonFormat(pattern="dd MMMM yyyy", timezone="Asia/Jakarta")
    private LocalDateTime openedAt;

    @JsonFormat(pattern="dd MMMM yyyy", timezone="Asia/Jakarta")
    private LocalDateTime createdAt;

    private Long depositoAccountId;
    private String accountNumber;
    private String customerId;
    private BigDecimal principalAmount;

    @JsonFormat(pattern="dd MMMM yyyy", timezone="Asia/Jakarta")
    private LocalDate maturityDate;
    private String accountStatus;
    private String rolloverOption;
}
