package com.core.banking.entity;

import com.core.banking.enums.LoanRepaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "loan_repayment_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentSchedule {
    @Id
    @Column(name = "loan_repayment_schedule_id")
    private String loanRepaymentScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_due", nullable = false)
    private BigDecimal principalDue;

    @Column(name = "interest_due", nullable = false)
    private BigDecimal interestDue;

    @Column(name = "total_due", nullable = false)
    private BigDecimal totalDue;

    @Column(name = "principal_paid")
    private BigDecimal principalPaid;

    @Column(name = "interest_paid")
    private BigDecimal interestPaid;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "payment_date")
    private Timestamp paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private LoanRepaymentStatus paymentStatus = LoanRepaymentStatus.PENDING;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
