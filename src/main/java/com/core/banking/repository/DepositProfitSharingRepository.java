package com.core.banking.repository;

import com.core.banking.entity.DepositAccount;
import com.core.banking.entity.DepositProfitSharing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepositProfitSharingRepository extends JpaRepository<DepositProfitSharing, Long> {
    boolean existsByDepositAccountAndProfitPeriodStartDateAndProfitPeriodEndDate(DepositAccount account, LocalDate start, LocalDate end);

    List<DepositProfitSharing> findByDepositAccount_DepositoAccountId(Long depositAcountId);
}
