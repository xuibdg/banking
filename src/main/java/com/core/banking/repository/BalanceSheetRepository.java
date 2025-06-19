package com.core.banking.repository;

import com.core.banking.entity.BalanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BalanceSheetRepository extends JpaRepository<BalanceSheet, String> {
    List<BalanceSheet> findBySystemAt(LocalDate systemAt);
    void deleteBySystemAt(LocalDate systemAt);
}
