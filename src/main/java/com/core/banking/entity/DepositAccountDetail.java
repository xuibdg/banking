package com.core.banking.entity;

import com.core.banking.enums.DepositoTransactionType;
import com.core.banking.enums.MutationType;
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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_account_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositAccountDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposito_account_detail_id")
    private Long depositoAccountDetailId;

//    @Column(name = "deposito_account_id", nullable = false)
//    private Long depositoAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposito_account_id", nullable = false)
    private DepositAccount depositAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private DepositoTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mutation_type", nullable = false)
    private MutationType mutationType;

    @Column(name = "nominal_transaction", nullable = false, precision = 15, scale = 2)
    private BigDecimal nominalTransaction;

    @Column(name = "transaction_reference", unique = true, length = 100)
    private String transactionReference;

    @Column(name = "begin_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal beginBalance;

    @Column(name = "end_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal endBalance;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_at")
    private LocalDateTime transactionAt = LocalDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}