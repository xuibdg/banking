package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SavingAccountRequest {

    private String customerId;
    private String savingTypeConfigId;
}
