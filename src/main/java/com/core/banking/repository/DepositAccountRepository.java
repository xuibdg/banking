package com.core.banking.repository;

import com.core.banking.entity.DepositAccount;
import com.core.banking.enums.DepositAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepositAccountRepository extends JpaRepository<DepositAccount, Long> {
    boolean existsByAccountNumber(String accountNumber);

    Optional<DepositAccount> findByAccountNumber(String accountNumber);

    List<DepositAccount> findByCustomerId(String customerId);

    Optional<DepositAccount> findFirstByAccountStatusOrderByDepositoAccountIdAsc(DepositAccountStatus accountStatus);

    List<DepositAccount> findByAccountStatus(DepositAccountStatus accountStatus);

    Optional<DepositAccount> findByDepositoAccountId(Long depositoAccountId);

    List<DepositAccount> findByAccountStatusAndMaturityDateLessThanEqual(DepositAccountStatus depositAccountStatus, LocalDate maturityDate);
}