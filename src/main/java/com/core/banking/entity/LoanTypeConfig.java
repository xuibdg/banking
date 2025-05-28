package com.core.banking.entity;

import com.core.banking.enums.LoanFeeType;
import com.core.banking.enums.Frequency;
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
@Table(name = "loan_type_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTypeConfig {
    @Id
    @Column(name = "loan_type_config_id", nullable = false)
    private String loanTypeConfigId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_type_id", nullable = false)
    private LoanType loanType;

    @Column(name = "min_loan_amount", nullable = false)
    private BigDecimal minLoanAmount;

    @Column(name = "max_loan_amount", nullable = false)
    private BigDecimal maxLoanAmount;

    @Column(name = "min_duration_months", nullable = false)
    private Integer minDurationMonths;

    @Column(name = "max_duration_months", nullable = false)
    private Integer maxDurationMonths;

    @Column(name = "interest_rate_pa", nullable = false)
    private BigDecimal interestRatePa;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_frequency", nullable = false)
    private Frequency repaymentFrequency = Frequency.MONTHLY;

    @Column(name = "late_payment_fee")
    private BigDecimal latePaymentFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "late_payment_fee_type")
    private LoanFeeType latePaymentFeeType = LoanFeeType.FIXED;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
