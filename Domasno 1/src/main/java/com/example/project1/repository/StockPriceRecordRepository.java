package com.example.project1.repository;

import com.example.project1.entity.StockPriceRecord;
import com.example.project1.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StockPriceRecordRepository extends JpaRepository<StockPriceRecord, Long> {
    Optional<StockPriceRecord> findByDateAndCompany(LocalDate date, Stock stock);
}
