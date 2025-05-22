package com.core.banking.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerRequest {
    private String nik;
    private String fullName;
    private String address;
    private String phoneNumber;
    private String email;
    private LocalDate dateOfBirth;
}
