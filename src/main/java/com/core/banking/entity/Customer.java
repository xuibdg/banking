package com.core.banking.entity;

import com.core.banking.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id")
    private String id;

    @Column(name = "nik", nullable = false, unique = true, length = 20)
    private String nik;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false)
    private CustomerStatus customerStatus = CustomerStatus.ACTIVE;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
