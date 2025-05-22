package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "deposit_accounts")
@Getter
@Setter
@Data
@AllArgsConstructor
public class DepositAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String accountNumber;

    private String customerName;

    @OneToMany(mappedBy = "depositAccount", cascade = CascadeType.ALL)
    private List<DepositAccountDetail> details;

    
}
