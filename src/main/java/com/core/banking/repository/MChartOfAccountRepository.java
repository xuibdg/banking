package com.core.banking.repository;

import com.core.banking.entity.MChartOfAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MChartOfAccountRepository extends JpaRepository<MChartOfAccount, String> {
    // Custom query untuk cek apakah kode sudah ada (lebih efisien dari findByCode)
    boolean existsByCode(String code);

    // Optional untuk mencari parent
    Optional<MChartOfAccount> findByCode(String code);
}
