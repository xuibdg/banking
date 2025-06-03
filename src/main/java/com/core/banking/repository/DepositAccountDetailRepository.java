package com.core.banking.repository;

import com.core.banking.entity.DepositAccount;
import com.core.banking.entity.DepositAccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositAccountDetailRepository extends JpaRepository<DepositAccountDetail, Long> {
    List<DepositAccountDetail> findByDepositAccountOrderByCreatedAtDesc(DepositAccount depositAccount);
}
