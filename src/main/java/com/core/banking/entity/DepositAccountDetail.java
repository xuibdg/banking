package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "deposit_account_details")
@Getter
@Setter
@Data
@AllArgsConstructor
public class DepositAccountDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String detailType;

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "deposit_account_id")
    private DepositAccount depositAccount;

    
}
