package com.core.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "saving_account_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingAccountDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saving_account_detail_id")
    private Long savingAccountDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_account_id", nullable = false)
    private SavingAccount savingAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private SavingTransactionType transactionType;

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

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "transaction_at")
    private OffsetDateTime transactionAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public enum SavingTransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, INTEREST_CREDIT, FEE_DEBIT, INITIAL_DEPOSIT
    }

    public enum MutationType {
        CREDIT, DEBIT
    }
}
