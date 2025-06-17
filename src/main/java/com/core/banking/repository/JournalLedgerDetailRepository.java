package com.core.banking.repository;

import com.core.banking.dto.EodReporting;
import com.core.banking.entity.JournalLedgerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JournalLedgerDetailRepository extends JpaRepository<JournalLedgerDetail, String> {

    @Query("SELECT j FROM JournalLedgerDetail j WHERE j.journalLedger.id = :id")
    List<JournalLedgerDetail> findAllByLedgerId(@Param("id") String id);
    List<JournalLedgerDetail> findByCoaIdIn(List<String> coaCodes);

    @Query("""
SELECT new com.core.banking.dto.EodReporting(
        d.coaCode,
        c.coaName,
        c.category,
        SUM(COALESCE(d.debit, 0) - COALESCE(d.credit, 0))
    )
    FROM JournalLedgerDetail d
    JOIN Coa c ON d.coaCode = c.coaCode
    WHERE d.journalLedger.createdAt BETWEEN :startDate AND :endDate
      AND c.category IN :categories
    GROUP BY d.coaCode, c.coaName, c.category
""")
    List<EodReporting> findEodReport(@Param("startDate")LocalDateTime start,
                                     @Param("endDate") LocalDateTime end,
                                     @Param ("categories") List<String> categories);
}
