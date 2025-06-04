package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposito_profit_sharings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositProfitSharing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposito_profit_sharing_id")
    private Long depositoProfitSharingId;

    @ManyToOne
    @JoinColumn(name = "deposito_account_id", nullable = false)
    private DepositAccount depositAccount;

    @ManyToOne
    @JoinColumn(name = "deposito_account_detail_id")
    private DepositAccountDetail depositAccountDetail;

    @Column(name = "profit_period_start_date", nullable = false)
    private LocalDate profitPeriodStartDate;

    @Column(name = "profit_period_end_date", nullable = false)
    private LocalDate profitPeriodEndDate;

    @Column(name = "nominal_profit_shared", nullable = false)
    private BigDecimal nominalProfitShared;

    @Column(name = "payout_date", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime payoutDate;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
