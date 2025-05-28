package com.core.banking.dto;

import com.core.banking.enums.SavingTypeStatus;
import lombok.*;

import java.sql.Timestamp;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingTypeRequest {
    private SavingTypeStatus typeName;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
