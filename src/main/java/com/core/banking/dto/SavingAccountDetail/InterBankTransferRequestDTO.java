package com.core.banking.dto.SavingAccountDetail;

import com.core.banking.enums.InterBankTransferMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterBankTransferRequestDTO {

    private String sourceAccountNumber;
    private String destinationBankCode;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private InterBankTransferMethod transferMethod;
    private String description;
    private String channel;
}