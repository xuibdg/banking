package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "deposit_type_configs")
@Getter
@Setter
@Data
@AllArgsConstructor
public class DepositTypeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String configName;

    private String configValue;

    @ManyToOne
    @JoinColumn(name = "deposit_type_id")
    private DepositType depositType;

}
