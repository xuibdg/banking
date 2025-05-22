package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Entity
@Table(name = "saving_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "saving_type_id")
    private String savingTypeId;

    @Column(name = "type_name", nullable = false, unique = true, length = 50)
    private String typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
