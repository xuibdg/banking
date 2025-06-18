package com.core.banking.repository;

import com.core.banking.entity.ProfitLoss;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProfitLossRepository extends JpaRepository<ProfitLoss, String> {
    List<ProfitLoss> findBySystemAt(LocalDate systemAt);
    void deleteBySystemAt(LocalDate systemAt);
}
