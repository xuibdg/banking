package com.core.banking.entity;

import com.core.banking.enums.EscrowTransactionType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "escrow_account_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowAccountDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "escrow_account_detail_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escrow_account_id", nullable = false)
    private EscrowAccount escrowAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private EscrowTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mutation_type", nullable = false)
    private MutationType mutationType;

    @Column(name = "nominal_transaction", precision = 15, scale = 2, nullable = false)
    private BigDecimal nominalTransaction;

    @Column(name = "begin_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal beginBalance;

    @Column(name = "end_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal endBalance;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_reference", unique = true, length = 100)
    private String transactionReference;

    @Column(name = "transaction_at")
    private Timestamp transactionAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "created_by")
    private String createBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

}
