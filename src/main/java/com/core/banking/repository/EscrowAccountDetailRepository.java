package com.core.banking.repository;

import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.EscrowAccountDetail;
import com.core.banking.enums.EscrowTransactionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface EscrowAccountDetailRepository extends JpaRepository<EscrowAccountDetail, String> {

    @EntityGraph(attributePaths = {"escrowAccount"})
    List<EscrowAccountDetail> findAll();

    @EntityGraph(attributePaths = {"escrowAccount"})
    @Query("SELECT ed FROM EscrowAccountDetail ed WHERE " +
            "(:id is NULL or ed.id = :id) AND " +
            "(:startDate is NULL OR ed.createdAt >= :startDate) AND " +
            "(:endDate is NULL or ed.createdAt <= :endDate) AND " +
            "(:transactionType is NULL or ed.transactionType = :transactionType) AND " +
            "ed.isDeleted = false")
    List<EscrowAccountDetail> findByNeedData(
            @Param("id") String id,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("transactionType")EscrowTransactionType transactionType
    );


}
