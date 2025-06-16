package com.core.banking.dto;

import com.core.banking.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MChartOfAccountResponse {

    private String id;
    private String code;
    private String name;
    private AccountType type;
    private String parentCode;
    private Boolean isActive;
}
