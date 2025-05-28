package com.core.banking.dto.SavingAccountDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementRequestDTO {

    private String savingAccountNumber;
    private String startDate;
    private String endDate;
    private int page = 0;
    private int size = 10;
}