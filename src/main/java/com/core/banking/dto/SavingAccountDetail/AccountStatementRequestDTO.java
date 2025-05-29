package com.core.banking.dto.SavingAccountDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementRequestDTO {

    private String savingAccountNumber;
    private Timestamp startDate;
    private Timestamp endDate;
    private int page = 0;
    private int size = 10;
}