package com.core.banking.dto;

import com.core.banking.entity.DepositAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositAccountResponse {
    private String customerName;
    private String depositTypeName;
    private BigDecimal profitSharePercentage;
    private Integer termInMonths;
    private LocalDateTime openedAt;
    private LocalDateTime createdAt;
    private Long depositoAccountId;
    private String accountNumber;
    private String customerId;
    private BigDecimal principalAmount;
    private LocalDate maturityDate;
    private String accountStatus;
    private String rolloverOption;

    public DepositAccountResponse(DepositAccount depositAccount) {
        this.depositoAccountId = depositAccount.getDepositoAccountId();
        this.accountNumber = depositAccount.getAccountNumber();
        this.customerId = depositAccount.getCustomer().getId();
        this.principalAmount = depositAccount.getPrincipalAmount();
        this.maturityDate = depositAccount.getMaturityDate();
        this.accountStatus = depositAccount.getAccountStatus().name();
        this.rolloverOption = depositAccount.getRolloverOption();
        this.openedAt = depositAccount.getOpenedAt();
        this.createdAt = depositAccount.getCreatedAt();

        if (depositAccount.getCustomer() != null) {
            this.customerName = depositAccount.getCustomer().getFullName();
        }

        if (depositAccount.getDepositTypeConfig() != null &&
                depositAccount.getDepositTypeConfig().getDepositType() != null) {
            this.depositTypeName = depositAccount.getDepositTypeConfig().getDepositType().getTypeName();
            this.profitSharePercentage = depositAccount.getDepositTypeConfig().getProfitSharePercentagePa();
            this.termInMonths = depositAccount.getDepositTypeConfig().getTermInMonths();
        }
    }
}
