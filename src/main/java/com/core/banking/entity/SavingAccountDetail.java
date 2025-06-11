package com.core.banking.entity;

import com.core.banking.enums.MutationType;
import com.core.banking.enums.SavingTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "saving_account_detail_id")
    private String savingAccountDetailId;

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

}
