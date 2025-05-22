package com.core.banking.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "deposit_accounts")
public class DepositAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String customerName;

    @OneToMany(mappedBy = "depositAccount", cascade = CascadeType.ALL)
    private List<DepositAccountDetail> details;

    // ...getter & setter...
}
