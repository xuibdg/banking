package com.core.banking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "deposit_type_configs")
public class DepositTypeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String configName;

    private String configValue;

    @ManyToOne
    @JoinColumn(name = "deposit_type_id")
    private DepositType depositType;

    // ...getter & setter...
}
