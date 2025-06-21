package com.core.banking.repository;

import com.core.banking.entity.JournalLedger;
import com.core.banking.entity.SavingAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalLedgerRepository extends JpaRepository<JournalLedger, Long> {
    Optional<Object> findById(String journalLedgerId);
    long countBySystemDate(LocalDate now);
}
