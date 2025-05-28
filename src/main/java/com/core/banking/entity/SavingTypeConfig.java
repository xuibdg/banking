package com.core.banking.entity;

import com.core.banking.enums.Frequency;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "saving_type_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingTypeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "saving_type_config_id")
    private String savingTypeConfigId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "saving_type_id", nullable = false)
    private SavingType savingType;

    @Column(name = "min_initial_deposit")
    private BigDecimal minInitialDeposit;

    @Column(name = "min_balance_limit")
    private BigDecimal minBalanceLimit;

    @Column(name = "max_balance_limit")
    private BigDecimal maxBalanceLimit;

    @Column(name = "daily_transaction_limit")
    private BigDecimal dailyTransactionLimit;

    @Column(name = "daily_transaction_count_limit")
    private Integer dailyTransactionCountLimit;

    @Column(name = "interest_rate_pa")
    private BigDecimal interestRatePa;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_calculation_frequency")
    private Frequency interestCalculationFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_payout_frequency")
    private Frequency interestPayoutFrequency;

    @Column(name = "monthly_maintenance_fee")
    private BigDecimal monthlyMaintenanceFee;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
