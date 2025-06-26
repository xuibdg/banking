package com.core.banking.dto.SavingAccountDetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDTO {

    private String savingAccountNumber;
    private BigDecimal amount;
    private String description;
    private String channel;
    @JsonProperty("isJournal")
    private boolean isJournal;
}
