package com.core.banking.dto;

import com.core.banking.enums.SavingAccountStatus;
import lombok.Data;

@Data
public class UpdateSavingAccountStatusRequest {
    private Long accountId;
    private SavingAccountStatus newStatus;
}
