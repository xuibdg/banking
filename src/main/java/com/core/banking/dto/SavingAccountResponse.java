package com.core.banking.dto;

import com.core.banking.entity.SavingType;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.enums.SavingAccountStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jdk.jshell.Snippet;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SavingAccountResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String accountNumber;
    private String customerId;
    private String customerName;
    private String savingTypeConfig;
    private String savingTypeName;
    private BigDecimal balance;
    private SavingAccountStatus status;

}
