package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "saving_type_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingTypeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saving_type_config_id")
    private Long savingTypeConfigId;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public enum Frequency {
        DAILY, MONTHLY, QUARTERLY, ANNUALLY, AT_MATURITY
    }
}
