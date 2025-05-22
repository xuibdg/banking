package com.core.banking.repository;

import com.core.banking.entity.SavingAccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountDetailRepository extends JpaRepository<SavingAccountDetail, String> {
}
