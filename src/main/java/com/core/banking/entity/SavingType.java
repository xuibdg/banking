package com.core.banking.entity;

import com.core.banking.enums.SavingTypeStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;


import java.sql.Timestamp;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "type_name", nullable = false, unique = true, length = 50)
    private SavingTypeStatus typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
