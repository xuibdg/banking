package com.core.banking.entity;

import com.core.banking.enums.SavingAccountStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "saving_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "saving_account_id")
    private String savingAccountId;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_type_config_id", nullable = false)
    private SavingTypeConfig savingTypeConfig;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "accrued_interest")
    private BigDecimal accruedInterest;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private SavingAccountStatus accountStatus = SavingAccountStatus.ACTIVE;

    @Column(name = "opened_at")
    private Timestamp openedAt;

    @Column(name = "closed_at")
    private Timestamp closedAt;

    @Column(name = "last_transaction_at")
    private Timestamp lastTransactionAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
