package com.core.banking.dto;

import com.core.banking.enums.TypeSenderBank;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EscrowRequestToPGRequest {
    private String title;
    private String type;
    private BigDecimal amount;
    private Integer step;
    private String senderBank;
    private String senderBankType;
    private String senderName;
    private String senderEmail;
    private String customerPhone;
    private String customerAddress;
    private String escrowAccountId;
    private String paymentGatewayId;
    private String internalReferenceId;

}
