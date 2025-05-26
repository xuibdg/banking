package com.core.banking.repository;

import com.core.banking.entity.SavingAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, String> {
    Optional<SavingAccount> getByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String number);

    @EntityGraph(attributePaths = {"savingTypeConfig", "customer", "savingTypeConfig.savingType"})
    Optional<SavingAccount> findByAccountNumber(String accountNumber);

    @EntityGraph(attributePaths = {"savingTypeConfig", "customer", "savingTypeConfig.savingType"})
    List<SavingAccount> findAll();

    @EntityGraph(attributePaths = {"customer", "savingTypeConfig", "savingTypeConfig.savingType"})
    Optional<SavingAccount> findById(String id);
}
