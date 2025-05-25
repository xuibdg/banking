package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deposit_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposito_type_id")
    private Long depositoTypeId;

    @OneToMany(mappedBy = "depositType", cascade = CascadeType.ALL)
    private List<DepositTypeConfig> depositTypeConfigs = new ArrayList<>();

    @Column(name = "type_name", length = 50, nullable = false, unique = true)
    private String typeName;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}