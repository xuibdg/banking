package com.core.banking.entity;


import com.core.banking.enums.SavingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "saving_configurations")
public class SavingConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Saving type is required")
    @Column(nullable = false, unique = true)
    private SavingType savingType;

    @NotNull(message = "Minimum limit is required")
    @Column(nullable = false)
    private BigDecimal minLimit;

    @NotNull(message = "Maximum limit is required")
    @DecimalMin(value = "0.0", message = "Maximum limit must be >= 0")
    @Column(nullable = false)
    private BigDecimal maxLimit;

    @NotNull(message = "Daily transaction limit is required")
    @DecimalMin(value = "0.0", message = "Daily transaction limit must be >= 0")
    @Column(nullable = false, name = "limit_transaction_daily")
    private BigDecimal limitTransactionDaily;

    @DecimalMin(value = "0.0", message = "Interest rate must be >= 0")
    @DecimalMax(value = "100.0", message = "Interest rate must be <= 100")
    private BigDecimal interestRate;

    @DecimalMin(value = "0.0", message = "Monthly fee must be >= 0")
    private BigDecimal monthlyFee;

    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    private void validateLimits() {
        if (maxLimit.compareTo(minLimit) < 0) {
            throw new IllegalArgumentException("Maximum limit must be >= minimum limit");
        }
    }

}
