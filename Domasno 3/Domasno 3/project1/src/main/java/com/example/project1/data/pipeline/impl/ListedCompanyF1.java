package com.example.project1.data.pipeline.impl;

import com.example.project1.data.pipeline.Filter;
import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.repository.ListedCompanyRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class ListedCompanyF1 implements Filter<List<ListedCompanyEntity>> {

    private final ListedCompanyRepository listedCompanyRepository;

    public ListedCompanyF1(ListedCompanyRepository listedCompanyRepository) {
        this.listedCompanyRepository = listedCompanyRepository;
    }

    private static final String STOCK_MARKET_URL = "https://www.mse.mk/mk/stats/symbolhistory/kmb";

    @Override
    public List<ListedCompanyEntity> execute(List<ListedCompanyEntity> input) throws IOException {
        Document document = Jsoup.connect(STOCK_MARKET_URL).get();
        Element selectMenu = document.select("select#Code").first();

        if (selectMenu != null) {
            Elements options = selectMenu.select("option");
            for (Element option : options) {
                String code = option.attr("value");
                if (!code.isEmpty() && code.matches("^[a-zA-Z]+$")) {
                    if (listedCompanyRepository.findByCompanyCode(code).isEmpty()) {
                        listedCompanyRepository.save(new ListedCompanyEntity(code));
                    }
                }
            }
        }

        return listedCompanyRepository.findAll();
    }
}
