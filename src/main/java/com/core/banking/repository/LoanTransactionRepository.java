package com.core.banking.repository;

import com.core.banking.entity.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, String> {

    @Query("""
        SELECT COALESCE(SUM(t.principalComponent), 0)
        FROM LoanTransaction t
        WHERE t.loanAccount.id = :loanAccountId
        AND t.transactionType = 'REPAYMENT'
    """)
    BigDecimal getTotalPrincipalPaid(@Param("loanAccountId") String loanAccountId);

}
