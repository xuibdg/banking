package com.core.banking.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.core.banking.enums.LoanTransactionType;
import com.core.banking.enums.LoanRepaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransactionUpdateRequest {

    private BigDecimal amount;
    private LocalDate paymentDate;
    private LoanTransactionType type;
    private LoanRepaymentStatus repaymentStatus;
}