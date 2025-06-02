package com.core.banking.entity;

import com.core.banking.enums.LoanTransactionType;
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

@Entity
@Table(name = "loan_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransaction {
    @Id
    @Column(name = "loan_transaction_id")
    private String loanTransactionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_repayment_schedule_id")
    private LoanRepaymentSchedule loanRepaymentSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private LoanTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "principal_component")
    private BigDecimal principalComponent;

    @Column(name = "interest_component")
    private BigDecimal interestComponent;

    @Column(name = "fee_component")
    private BigDecimal feeComponent;

    @Column(name = "transaction_date")
    private Timestamp transactionDate;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_number", unique = true, length = 100)
    private String referenceNumber;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
