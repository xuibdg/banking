package com.core.banking.repository;

import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.EscrowAccountDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscrowAccountDetailRepository extends JpaRepository<EscrowAccountDetail, String> {

    @EntityGraph(attributePaths = {"escrowAccount"})
    List<EscrowAccountDetail> findAll();
}
