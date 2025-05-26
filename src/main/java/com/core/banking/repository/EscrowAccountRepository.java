package com.core.banking.repository;

import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.entity.EscrowAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, String> {

    long countByAccountNumberStartingWith(String prefix);

    @EntityGraph(attributePaths = {"payerCustomer", "beneficiaryCustomer"})
    List<EscrowAccount> findAll();


}
