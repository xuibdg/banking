package com.core.banking.repository;

import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingAccountDetail;
import com.core.banking.enums.MutationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;

import java.util.Optional;

@Repository
public interface SavingAccountDetailRepository extends JpaRepository<SavingAccountDetail, String> {

    @Query("SELECT sad FROM SavingAccountDetail sad " +
            "WHERE sad.savingAccount.savingAccountId = :savingAccountId " +
            "AND (:startDate IS NULL OR sad.transactionAt >= :startDate) " +
            "AND (:endDate IS NULL OR sad.transactionAt <= :endDate) " +
            "ORDER BY sad.transactionAt DESC")
    Page<SavingAccountDetail> findBySavingAccountAndDateRange(
            @Param("savingAccountId") String savingAccountId,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            Pageable pageable
    );
    @Query("SELECT COALESCE(SUM(sad.nominalTransaction), 0) FROM SavingAccountDetail sad " +
            "WHERE sad.savingAccount = :savingAccount " +
            "AND sad.mutationType = :mutationType " +
            "AND sad.transactionAt >= :startOfDay AND sad.transactionAt < :endOfDay")
    BigDecimal sumTransactionsByAccountAndMutationTypeAndDate(
            @Param("savingAccount") SavingAccount savingAccount,
            @Param("mutationType") MutationType mutationType,
            @Param("startOfDay") Timestamp startOfDay,
            @Param("endOfDay") Timestamp endOfDay
    );

    Optional<SavingAccountDetail> findByTransactionReference(String transactionReference);
}