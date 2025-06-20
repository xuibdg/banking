package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_profit_loss")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfitLoss {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coa_code", nullable = false)
    private MChartOfAccount coaCode;

    @Column(name = "coa_name")
    private String coaName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category", nullable = false)
    private MChartOfAccount category;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "system_at")
    private LocalDate systemAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
