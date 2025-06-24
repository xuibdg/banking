package com.core.banking.repository;

import com.core.banking.entity.SavingAccount;
import com.core.banking.enums.SavingAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingAccountRepository extends JpaRepository<SavingAccount, String> {
    boolean existsByCustomer_IdAndAccountStatus(String customerId, SavingAccountStatus accountStatus);
    boolean existsByAccountNumber(String number);

    @Query("SELECT sa FROM SavingAccount sa " +
        "JOIN FETCH sa.savingTypeConfig stc " +
        "JOIN FETCH stc.savingType " +
        "JOIN FETCH sa.customer " +
        "WHERE sa.accountNumber = :accountNumber")
    Optional<SavingAccount> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT sa FROM SavingAccount sa " +
            "JOIN FETCH sa.customer c " +
            "JOIN FETCH sa.savingTypeConfig stc " +
            "JOIN FETCH stc.savingType " +
            "WHERE c.fullName = :fullName")
    Optional<SavingAccount> findByfindByCustomerFullName(@Param("fullName") String fullName);

    @Query("SELECT sa FROM SavingAccount sa " +
            "JOIN FETCH sa.customer c " +
            "JOIN FETCH sa.savingTypeConfig stc " +
            "JOIN FETCH stc.savingType " +
            "WHERE c.nik = :nik")
    Optional<SavingAccount> findByCustomerNik(@Param("nik") String nik);

    @Query("SELECT sa FROM SavingAccount sa " +
            "JOIN FETCH sa.savingTypeConfig stc " +
            "JOIN FETCH stc.savingType " +
            "JOIN FETCH sa.customer")
    List<SavingAccount> findAll();

    @Query("SELECT sa FROM SavingAccount sa " +
            "JOIN FETCH sa.savingTypeConfig stc " +
            "JOIN FETCH stc.savingType " +
            "JOIN FETCH sa.customer " +
            "WHERE sa.id = :id")
    Optional<SavingAccount> findById(@Param("id") String id);

    SavingAccount findByCustomerId (String customer_id);

    @Query("SELECT sa FROM SavingAccount sa WHERE sa.accountNumber = :accountNumber")
    Optional<SavingAccount> findWithLockByAccountNumber(@Param("accountNumber") String accountNumber);

    List<SavingAccount> findByCustomer_Id(String customerId);
}
