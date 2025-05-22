package com.core.banking.repository;

import com.core.banking.entity.Customer;
import com.core.banking.enums.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByIdOrNik(String id, String nik);

    @Query(value = "select * from customers c where c.nik = :nik;", nativeQuery = true)
    Optional<Customer> findByNik(@Param("nik") String nik);

    boolean existsByEmailAndIdNot(String email, String id);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String id);

    Optional<Customer> findByCustomerStatus(CustomerStatus customerStatus);
}
