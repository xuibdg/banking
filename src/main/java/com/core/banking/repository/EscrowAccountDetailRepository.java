package com.core.banking.repository;

import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.EscrowAccountDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface EscrowAccountDetailRepository extends JpaRepository<EscrowAccountDetail, String> {

    @EntityGraph(attributePaths = {"escrowAccount"})
    List<EscrowAccountDetail> findAll();

}
