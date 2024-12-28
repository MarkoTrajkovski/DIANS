package com.example.project1.repository;

import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.db.TransactionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, Long> {
    Optional<TransactionHistoryEntity> findByDateAndCompany(LocalDate date, ListedCompanyEntity company);
    List<TransactionHistoryEntity> findByCompanyIdAndDateBetween(Long companyId, LocalDate from, LocalDate to);
    List<TransactionHistoryEntity> findByCompanyId(Long companyId);
}
