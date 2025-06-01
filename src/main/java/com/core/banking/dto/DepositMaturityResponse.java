package com.core.banking.dto;

import com.core.banking.enums.DepositAccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DepositMaturityResponse {
    private Long depositAccountId;
    private String accountNumber;
    private String customerName;
    private BigDecimal principalAmount;
    private BigDecimal profitAmount;
    private BigDecimal totalAmount;
    private LocalDate maturityDate;
    private DepositAccountStatus beforeStatus;
    private DepositAccountStatus afterStatus;
    private String rolloverOption;
    private String message;
    private boolean success;

    private Long newDepositAccountId;
    private String newAccountNumber;
}
