package com.example.project1.data.pipeline.impl;

import com.example.project1.data.Parser;
import com.example.project1.data.pipeline.Filter;
import com.example.project1.entity.StockPriceRecord;
import com.example.project1.entity.Stock;
import com.example.project1.repository.StockRepository;
import com.example.project1.repository.StockPriceRecordRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterTwo implements Filter<List<Stock>> {

    private final StockRepository stockRepository;
    private final StockPriceRecordRepository stockPriceRecordRepository;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public FilterTwo(StockRepository stockRepository, StockPriceRecordRepository stockPriceRecordRepository) {
        this.stockRepository = stockRepository;
        this.stockPriceRecordRepository = stockPriceRecordRepository;
    }

    public List<Stock> execute(List<Stock> input) throws IOException, ParseException {
        List<Stock> companies = new ArrayList<>();

        for (Stock stock : input) {
            if (stock.getLastUpdated() == null) {
                for (int i = 1; i <= 10; i++) {
                    int temp = i - 1;
                    LocalDate fromDate = LocalDate.now().minusYears(i);
                    LocalDate toDate = LocalDate.now().minusYears(temp);
                    addHistoricalData(stock, fromDate, toDate);
                }
            } else {
                companies.add(stock);
            }
        }

        return companies;
    }

    private void addHistoricalData(Stock stock, LocalDate fromDate, LocalDate toDate) throws IOException {
        Connection.Response response = Jsoup.connect(HISTORICAL_DATA_URL + stock.getCompanyCode())
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
                    LocalDate date = Parser.parseDate(columns.get(0).text(), "d.M.yyyy");

                    if (date != null && stockPriceRecordRepository.findByDateAndCompany(date, stock).isEmpty()) {
                        if (stock.getLastUpdated() == null || stock.getLastUpdated().isBefore(date)) {
                            stock.setLastUpdated(date);
                            stockRepository.save(stock);
                        }

                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        Double lastTransactionPrice = Parser.parseDouble(columns.get(1).text(), format);
                        Double maxPrice = Parser.parseDouble(columns.get(2).text(), format);
                        Double minPrice = Parser.parseDouble(columns.get(3).text(), format);
                        Double averagePrice = Parser.parseDouble(columns.get(4).text(), format);
                        Double percentageChange = Parser.parseDouble(columns.get(5).text(), format);
                        Integer quantity = Parser.parseInteger(columns.get(6).text(), format);
                        Integer turnoverBest = Parser.parseInteger(columns.get(7).text(), format);
                        Integer totalTurnover = Parser.parseInteger(columns.get(8).text(), format);

                        StockPriceRecord stockPriceRecord = new StockPriceRecord(
                                date, lastTransactionPrice, maxPrice, minPrice, averagePrice, percentageChange,
                                quantity, turnoverBest, totalTurnover);
                        stockPriceRecord.setStock(stock);

                        stockPriceRecordRepository.save(stockPriceRecord);
                    }
                }
            }
        }
    }

}
