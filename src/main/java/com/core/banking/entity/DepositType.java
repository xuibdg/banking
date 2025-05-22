package com.core.banking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "deposit_types")
public class DepositType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String typeName;

    private String description;

    // ...getter & setter...
}
