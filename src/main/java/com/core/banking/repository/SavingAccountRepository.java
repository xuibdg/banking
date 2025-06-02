package com.core.banking.repository;

import com.core.banking.entity.SavingAccount;
import com.core.banking.enums.SavingAccountStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface SavingAccountRepository extends JpaRepository<SavingAccount, String> {
    boolean existsByCustomer_IdAndAccountStatus(String customerId, SavingAccountStatus accountStatus);
    boolean existsByAccountNumber(String number);

    @EntityGraph(attributePaths = {"savingTypeConfig", "customer", "savingTypeConfig.savingType"})
    Optional<SavingAccount> findByAccountNumber(String accountNumber);

    @EntityGraph(attributePaths = {"savingTypeConfig", "customer", "savingTypeConfig.savingType"})
    List<SavingAccount> findAll();

    @EntityGraph(attributePaths = {"customer", "savingTypeConfig", "savingTypeConfig.savingType"})
    Optional<SavingAccount> findById(String id);

    SavingAccount findByCustomerId (String customer_id);
}
    Optional<SavingAccount> findAccountNumber(String accountNumber);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SavingAccount> findWithLockByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SavingAccount> findWithLockBySavingAccountId(String savingAccountId);
}