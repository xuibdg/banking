package com.core.banking.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanPaymentRequest {
    private BigDecimal paymentAmount;
    private LocalDateTime paidAt;
}
