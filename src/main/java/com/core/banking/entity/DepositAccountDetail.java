package com.core.banking.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "deposit_account_details")
public class DepositAccountDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String detailType;

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "deposit_account_id")
    private DepositAccount depositAccount;

    // ...getter & setter...
}
