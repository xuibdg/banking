package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "m_system")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;

    @Column(name = "name")
    private String name;

    @Column(name = "system_at")
    private LocalDate systemAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}
