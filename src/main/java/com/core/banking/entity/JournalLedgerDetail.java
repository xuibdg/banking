package com.core.banking.entity;

import com.core.banking.enums.MutationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "t_journal_ledger_detail")
public class JournalLedgerDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_ledger_id")
    private JournalLedger journalLedger;

    @Column(name = "coa_id", nullable = false)
    private String coaId;

    @Column(name = "coa_code", nullable = false, length = 20)
    private String coaCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "mutation_type", nullable = false)
    private MutationType mutationType;

    @Column(name = "debit", precision = 18, scale = 2)
    private BigDecimal debit;

    @Column(name = "credit", precision = 18, scale = 2)
    private BigDecimal credit;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;
}
