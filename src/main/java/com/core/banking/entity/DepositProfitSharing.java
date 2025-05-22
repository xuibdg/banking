package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "deposit_profit_sharing")
@Getter
@Setter
@Data
@AllArgsConstructor
public class DepositProfitSharing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Double profitAmount;

    private String period;

    @ManyToOne
    @JoinColumn(name = "deposit_account_id")
    private DepositAccount depositAccount;

}
