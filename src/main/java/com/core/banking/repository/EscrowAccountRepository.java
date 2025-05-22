package com.core.banking.repository;

import com.core.banking.entity.EscrowAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, Long> {
}
