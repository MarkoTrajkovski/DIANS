package com.example.project1.repository;

import com.example.project1.db.ListedCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListedCompanyRepository extends JpaRepository<ListedCompanyEntity, Long> {
    Optional<ListedCompanyEntity> findByCompanyCode(String companyCode);
}
