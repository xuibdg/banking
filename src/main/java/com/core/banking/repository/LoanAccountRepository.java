package com.core.banking.repository;

import com.core.banking.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, String> {
    Optional<LoanAccount> findById(String loanAccountId);

    @Query(
            value = """
    SELECT 
      la.loan_account_id,
      la.principal_amount,
      COALESCE(SUM(lt.principal_component), 0) AS total_principal_paid,
      (la.principal_amount - COALESCE(SUM(lt.principal_component), 0)) AS outstanding_principal
    FROM loan_accounts la
    LEFT JOIN loan_transactions lt 
      ON la.loan_account_id = lt.loan_account_id
      AND lt.transaction_type = 'REPAYMENT'
    WHERE la.loan_account_id = :loanAccountId
    GROUP BY la.loan_account_id, la.principal_amount
  """,
            nativeQuery = true
    )
    Map<String, Object> getOutstandingPrincipalByLoanAccountId(@Param("loanAccountId") Long loanAccountId);

}

