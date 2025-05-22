package com.core.banking.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "deposit_profit_sharing")
public class DepositProfitSharing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double profitAmount;

    private String period;

    @ManyToOne
    @JoinColumn(name = "deposit_account_id")
    private DepositAccount depositAccount;

    // ...getter & setter...
}
