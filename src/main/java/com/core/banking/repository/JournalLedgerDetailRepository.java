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

    @Query(value = """
    SELECT 
        d.coa_code as coaCode, 
        c.name as coaName, 
        c.category as category,
        SUM(COALESCE(d.debit, 0) - COALESCE(d.credit, 0)) as amount
    FROM t_journal_ledger_detail d
    JOIN m_chart_of_account c ON d.coa_code = c.code
    WHERE d.created_at BETWEEN :startDate AND :endDate
      AND c.category IN :categories
    GROUP BY d.coa_code, c.name, c.type, c.category
""", nativeQuery = true)
    List<EodReporting> findEodReport(@Param("startDate")LocalDateTime start,
                                     @Param("endDate") LocalDateTime end,
                                     @Param ("categories") List<String> categories);

    List<JournalLedgerDetail> findByJournalLedgerId(String journalLedgerId);

}
