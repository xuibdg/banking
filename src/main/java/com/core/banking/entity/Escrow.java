package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "escrow")
@Getter
@Setter
@Data
@AllArgsConstructor
public class Escrow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ...existing code...
}