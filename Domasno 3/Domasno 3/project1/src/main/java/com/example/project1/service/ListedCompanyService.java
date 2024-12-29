package com.example.project1.service;

import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.repository.ListedCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListedCompanyService {

    private final ListedCompanyRepository listedCompanyRepository;

    public List<ListedCompanyEntity> findAll() {
        return listedCompanyRepository.findAllByOrderByIdAsc();
    }

    public ListedCompanyEntity findById(Long id) throws Exception {
        return listedCompanyRepository.findById(id).orElseThrow(Exception::new);
    }

}
