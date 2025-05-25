package com.core.banking.entity;

import com.core.banking.enums.Frequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_type_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositTypeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposito_type_config_id")
    private Long depositoTypeConfigId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposito_type_id", nullable = false)
    private DepositType depositType;

//    @Column(name = "deposito_type_id", nullable = false)
//    private Long depositoTypeId;

    @Column(name = "min_deposit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal minDepositAmount;

    @Column(name = "term_in_months", nullable = false)
    private Integer termInMonths;

    @Column(name = "profit_share_percentage_pa", nullable = false, precision = 5, scale = 2)
    private BigDecimal profitSharePercentagePa;

    @Enumerated(EnumType.STRING)
    @Column(name = "profit_payout_frequency")
    private Frequency profitPayoutFrequency;

    @Column(name = "early_withdrawal_penalty_percentage", precision = 5, scale = 2)
    private BigDecimal earlyWithdrawalPenaltyPercentage = BigDecimal.ZERO;

    @Column(name = "rollover_allowed")
    private Boolean rolloverAllowed = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}