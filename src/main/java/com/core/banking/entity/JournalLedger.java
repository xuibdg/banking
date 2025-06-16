package com.core.banking.entity;

import com.core.banking.enums.JournalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "t_journal_ledger")
public class JournalLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "journal_code", nullable = false)
    private String journalCode;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JournalStatus status;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "system_date", nullable = false)
    private LocalDate systemDate;

    @Column(name = "total_debit", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDebit;

    @Column(name = "total_credit", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCredit;
    @Column(name = "is_posted", nullable = false)
    private Boolean isPosted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @OneToMany(mappedBy = "journalLedger", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalLedgerDetail> details = new ArrayList<>();
}
