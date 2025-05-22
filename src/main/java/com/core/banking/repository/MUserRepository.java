package com.core.banking.repository;

import com.core.banking.entity.MUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MUserRepository extends JpaRepository<MUser, String> {

    @Query("SELECT mu FROM MUser mu WHERE mu.name LIKE CONCAT('%', :name, '%')")
    List<MUser> findByName(String name);

    @Query("SELECT mu FROM MUser mu WHERE mu.name = :name AND mu.isDeleted = false")
    Optional<MUser> findByNameOptional(String name);

}
