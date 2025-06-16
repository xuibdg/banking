package com.core.banking.repository;

import com.core.banking.entity.JournalLedgerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalLedgerDetailRepository extends JpaRepository<JournalLedgerDetail, String> {

    @Query("SELECT j FROM JournalLedgerDetail j WHERE j.journalLedger.id = :id")
    List<JournalLedgerDetail> findAllByLedgerId(@Param("id") String id);
    List<JournalLedgerDetail> findByCoaIdIn(List<String> coaCodes);

}
