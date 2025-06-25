package com.core.banking.entity;

import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.TransactionTypeStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_customer_id", nullable = false)
    private Customer payerCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_customer_id", nullable = false)
    private Customer beneficiaryCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_account_id")
    private SavingAccount savingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id")
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_account_id")
    private DepositAccount depositAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_account_status", nullable = false)
    private TransactionTypeStatus transactionTypeStatus;

    @Column(name = "nominal_transaction", precision = 15, scale = 2, nullable = false)
    private BigDecimal nominalTransaction;

    @Column(name = "release_account_number")
    private String releaseAccountNumber;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "type_fund_escrow", nullable = false)
//    private TypeFundEscrow typeFundEscrow;

    @Column(name = "created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "is_deleted")
    private boolean isDeleted = false;
}
