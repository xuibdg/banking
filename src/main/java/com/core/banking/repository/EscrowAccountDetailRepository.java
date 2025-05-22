package com.core.banking.repository;

import com.core.banking.entity.EscrowAccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscrowAccountDetailRepository extends JpaRepository<EscrowAccountDetail, String> {
}
