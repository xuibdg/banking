package com.core.banking.entity;

import com.core.banking.enums.LoanAccountStatus;
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
import java.time.OffsetDateTime;

@Entity
@Table(name = "loan_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount {
    @Id
    @Column(name = "loan_account_id")
    private String loanAccountId;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_type_config_id", nullable = false)
    private LoanTypeConfig loanTypeConfig;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate_applied", nullable = false)
    private BigDecimal interestRateApplied;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "outstanding_principal", nullable = false)
    private BigDecimal outstandingPrincipal;

    @Column(name = "installment_amount")
    private BigDecimal installmentAmount;

    @Column(name = "disbursement_date")
    private java.time.LocalDate disbursementDate;

    @Column(name = "first_repayment_date")
    private java.time.LocalDate firstRepaymentDate;

    @Column(name = "last_repayment_date")
    private java.time.LocalDate lastRepaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private LoanAccountStatus accountStatus = LoanAccountStatus.PENDING_APPROVAL;

    @Column(name = "application_date")
    private Timestamp applicationDate;

    @Column(name = "closed_at")
    private Timestamp closedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
