package com.core.banking.entity;

import com.core.banking.enums.DepositAccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deposit_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposito_account_id")
    private Long depositoAccountId;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @OneToMany(mappedBy = "depositAccount", cascade = CascadeType.ALL)
    private List<DepositAccountDetail> depositAccountDetails = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "deposit_type_config_id", nullable = false)
    private DepositTypeConfig depositTypeConfig;

    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private DepositAccountStatus accountStatus = DepositAccountStatus.ACTIVE;

    @Column(name = "rollover_option", length = 50)
    private String rolloverOption;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "opened_at")
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
