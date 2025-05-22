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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "escrow_account_id")
    private Long escrowAccountId;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private EscrowAccountStatus accountStatus = EscrowAccountStatus.PENDING_FUNDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_customer_id")
    private Customer payerCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_customer_id")
    private Customer beneficiaryCustomer;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
