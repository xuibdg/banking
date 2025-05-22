package com.core.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMetaData {
    private String userId;
    private String username;
    private String email;
    private String roleId;
    private String authenticationKey;
    private String sessionId;
}
