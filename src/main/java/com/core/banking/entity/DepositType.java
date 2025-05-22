package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "deposit_types")
@Getter
@Setter
@Data
@AllArgsConstructor
public class DepositType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String typeName;

    private String description;

    
}
