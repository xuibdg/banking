package com.core.banking.repository;

import com.core.banking.entity.DepositAccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositAccountDetailRepository extends JpaRepository<DepositAccountDetail, Long> {
}
