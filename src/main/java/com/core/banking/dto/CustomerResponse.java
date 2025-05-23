package com.core.banking.dto;

import com.core.banking.enums.CustomerStatus;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {
    private String customerId;
    private String nik;
    private String fullName;
    private String address;
    private String phoneNumber;
    private String email;
    private LocalDate dateOfBirth;
    private CustomerStatus customerStatus;
    private Timestamp createdAt;
}
