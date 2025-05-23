package com.core.banking.dto;

import com.core.banking.enums.LoanRepaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentScheduleRequest {

    private String loanAccountId; // Relasi ke LoanAccount

    private Integer installmentNumber;
    private LocalDate dueDate;

    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal totalDue;

    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal amountPaid;

    private OffsetDateTime paymentDate;

    private LoanRepaymentStatus paymentStatus;

    // Biasanya `createdAt` dan `updatedAt` diset otomatis di service, jadi bisa di-skip dari request
}

