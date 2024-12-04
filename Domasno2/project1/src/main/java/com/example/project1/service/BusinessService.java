package com.example.project1.service;

import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.db.TransactionHistoryEntity;
import com.example.project1.repository.TransactionHistoryRepository;
import com.example.project1.repository.ListedCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final ListedCompanyRepository listedCompanyRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    public List<ListedCompanyEntity> findAll() {
        return listedCompanyRepository.findAll();
    }

    public ListedCompanyEntity findById(Long id) throws Exception {
        return listedCompanyRepository.findById(id).orElseThrow(Exception::new);
    }

    public List<TransactionHistoryEntity> findAllToday() {
        return transactionHistoryRepository.findAllByDate(LocalDate.now());
    }

}
