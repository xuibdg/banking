package com.core.banking.dto;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MUserResponse {
    private String id;
    private String userId;
    private String name;
    private String email;
    private Timestamp createdAt;
    private String roleName;
}
