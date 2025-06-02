package com.core.banking.repository;

import com.core.banking.entity.EscrowAccount;
import com.core.banking.enums.EscrowAccountStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, String> {
    Optional<EscrowAccount> findByAccountNumber(String accountNumber);

    long countByAccountNumberStartingWith(String prefix);

    @EntityGraph(attributePaths = {"payerCustomer", "beneficiaryCustomer"})
    List<EscrowAccount> findAll();

    @EntityGraph(attributePaths = {"payerCustomer", "beneficiaryCustomer"})
    @Query("SELECT e FROM EscrowAccount e WHERE " +
            "(:id is NULL or e.id = :id) AND " +
            "(:startDate is NULL OR e.createdAt >= :startDate) AND " +
            "(:endDate is NULL or e.createdAt <= :endDate) AND " +
            "(:accountStatus is NULL or e.accountStatus = :accountStatus) AND " +
            "e.isDeleted = false")
    List<EscrowAccount> findByNeedData(
            @Param("id") String id,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("accountStatus")EscrowAccountStatus accountStatus
            );


}

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EscrowAccount> findWithLockByAccountNumber(String accountNumber);
}