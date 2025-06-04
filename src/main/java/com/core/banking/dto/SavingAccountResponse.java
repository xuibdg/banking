package com.core.banking.dto;

import com.core.banking.enums.SavingAccountStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    private String nik;
    private String savingTypeConfigId;
    private String savingTypeName;
    private BigDecimal currentBalance;
    private BigDecimal accruedInterest;
    private SavingAccountStatus status;
    private Boolean isDeleted;

}
