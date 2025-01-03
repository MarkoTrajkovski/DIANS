package com.example.project1.data.pipeline.impl;

import com.example.project1.data.DataParser;
import com.example.project1.data.pipeline.Filter;
import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.db.TransactionHistoryEntity;
import com.example.project1.repository.ListedCompanyRepository;
import com.example.project1.repository.TransactionHistoryRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryF2 implements Filter<List<ListedCompanyEntity>> {

    private final ListedCompanyRepository listedCompanyRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public TransactionHistoryF2(ListedCompanyRepository listedCompanyRepository, TransactionHistoryRepository transactionHistoryRepository) {
        this.listedCompanyRepository = listedCompanyRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    public List<ListedCompanyEntity> execute(List<ListedCompanyEntity> input) throws IOException {
        List<ListedCompanyEntity> companies = new ArrayList<>();

        for (ListedCompanyEntity company : input) {
            if (company.getLastUpdated() == null) {
                for (int i = 1; i <= 10; i++) {
                    int temp = i - 1;
                    LocalDate fromDate = LocalDate.now().minusYears(i);
                    LocalDate toDate = LocalDate.now().minusYears(temp);
                    addHistoricalData(company, fromDate, toDate);
                }
            } else {
                companies.add(company);
            }
        }

        return companies;
    }

    private void addHistoricalData(ListedCompanyEntity company, LocalDate fromDate, LocalDate toDate) throws IOException {
        Connection.Response response = Jsoup.connect(HISTORICAL_DATA_URL + company.getCompanyCode())
                .data("FromDate", fromDate.toString())
                .data("ToDate", toDate.toString())
                .method(Connection.Method.POST)
                .execute();

        Document document = response.parse();

        Element table = document.select("table#resultsTable").first();

        if (table != null) {
            Elements rows = table.select("tbody tr");

            for (Element row : rows) {
                Elements columns = row.select("td");

                if (columns.size() > 0) {
                    LocalDate date = DataParser.parseDate(columns.get(0).text(), "d.M.yyyy");

                    if (transactionHistoryRepository.findByDateAndCompany(date, company).isEmpty()) {


                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        Double lastTransactionPrice = DataParser.parseDouble(columns.get(1).text(), format);
                        Double maxPrice = DataParser.parseDouble(columns.get(2).text(), format);
                        Double minPrice = DataParser.parseDouble(columns.get(3).text(), format);
                        Double averagePrice = DataParser.parseDouble(columns.get(4).text(), format);
                        Double percentageChange = DataParser.parseDouble(columns.get(5).text(), format);
                        Integer quantity = DataParser.parseInteger(columns.get(6).text(), format);
                        Integer turnoverBest = DataParser.parseInteger(columns.get(7).text(), format);
                        Integer totalTurnover = DataParser.parseInteger(columns.get(8).text(), format);

                        if (maxPrice != null) {

                            if (company.getLastUpdated() == null || company.getLastUpdated().isBefore(date)) {
                                company.setLastUpdated(date);
                            }

                            TransactionHistoryEntity transactionHistoryEntity = new TransactionHistoryEntity(
                                    date, lastTransactionPrice, maxPrice, minPrice, averagePrice, percentageChange,
                                    quantity, turnoverBest, totalTurnover);
                            transactionHistoryEntity.setCompany(company);
                            transactionHistoryRepository.save(transactionHistoryEntity);
                            company.getHistoricalData().add(transactionHistoryEntity);
                        }
                    }
                }
            }
        }

        listedCompanyRepository.save(company);
    }

}
