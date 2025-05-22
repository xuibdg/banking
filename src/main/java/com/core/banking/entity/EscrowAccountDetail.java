package com.core.banking.entity;

import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.entity.EscrowAccount;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "escrow_account_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowAccountDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "escrow_account_detail_id")
    private Long escrowAccountDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escrow_account_id", nullable = false)
    private EscrowAccount escrowAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private EscrowTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mutation_type", nullable = false)
    private MutationType mutationType;

    @Column(name = "nominal_transaction", nullable = false)
    private BigDecimal nominalTransaction;

    @Column(name = "begin_balance", nullable = false)
    private BigDecimal beginBalance;

    @Column(name = "end_balance", nullable = false)
    private BigDecimal endBalance;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_reference", unique = true, length = 100)
    private String transactionReference;

    @Column(name = "transaction_at")
    private OffsetDateTime transactionAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public enum MutationType {
        CREDIT, DEBIT
    }
}
