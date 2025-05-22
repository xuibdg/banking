package com.core.banking.entity;

import com.core.banking.enums.EscrowAccountStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "escrow_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "escrow_account_id")
    private String id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private EscrowAccountStatus accountStatus = EscrowAccountStatus.PENDING_FUNDING;

    @Column(name = "payer_customer_id")
    private String payerCustomerId;

    @Column(name = "beneficiary_customer_id")
    private String beneficiaryCustomerId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
