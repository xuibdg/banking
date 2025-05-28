package com.core.banking.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "deposit_accounts")
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String accountNumber;

    private String customerName;

    @OneToMany(mappedBy = "depositAccount", cascade = CascadeType.ALL)
    private List<DepositAccountDetail> details;

}
