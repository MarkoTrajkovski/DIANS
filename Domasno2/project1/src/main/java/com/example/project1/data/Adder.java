package com.example.project1.data;

import com.example.project1.data.pipeline.Pipe;
import com.example.project1.data.pipeline.impl.ListedCompanyF1;
import com.example.project1.data.pipeline.impl.TransactionHistoryF2;
import com.example.project1.data.pipeline.impl.TransactionHistoryF3;
import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.repository.ListedCompanyRepository;
import com.example.project1.repository.TransactionHistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Adder {

    private final ListedCompanyRepository listedCompanyRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    @PostConstruct
    private void initializeData() throws IOException, ParseException {
        long startTime = System.nanoTime();

        Pipe<List<ListedCompanyEntity>> pipe = new Pipe<>();
        pipe.addFilter(new ListedCompanyF1(listedCompanyRepository));
        pipe.addFilter(new TransactionHistoryF2(listedCompanyRepository, transactionHistoryRepository));
        pipe.addFilter(new TransactionHistoryF3(listedCompanyRepository, transactionHistoryRepository));
        pipe.runFilter(null);

        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;

        long hours = durationInMillis / 3_600_000;
        long minutes = (durationInMillis % 3_600_000) / 60_000;
        long seconds = (durationInMillis % 60_000) / 1_000;

        System.out.printf("Total time for all filters to complete: %02d hours, %02d minutes, %02d seconds%n", hours, minutes, seconds);
    }

}
