package com.example.project1.web;

import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.db.TransactionHistoryEntity;
import com.example.project1.service.ListedCompanyService;
import com.example.project1.service.TechAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ListedCompanyController {

    private final ListedCompanyService listedCompanyService;
    private final TechAnalysisService techAnalysisService;

    @GetMapping("/")
    public String getCompaniesPage(Model model) {
        model.addAttribute("companies", listedCompanyService.findAll());
        return "companies";
    }

    @GetMapping("/company")
    public String getCompanyPage(@RequestParam(name = "companyId") Long companyId, Model model) throws Exception {
        List<Map<String, Object>> companyData = new ArrayList<>();
        ListedCompanyEntity company = listedCompanyService.findById(companyId);

        Map<String, Object> data = new HashMap<>();
        data.put("companyCode", company.getCompanyCode());
        data.put("lastUpdated", company.getLastUpdated());

        List<LocalDate> dates = new ArrayList<>();
        List<Double> prices = new ArrayList<>();

        for (TransactionHistoryEntity historicalData : company.getHistoricalData()) {
            dates.add(historicalData.getDate());
            prices.add(historicalData.getLastTransactionPrice());
        }

        data.put("dates", dates);
        data.put("prices", prices);
        data.put("id", company.getId());
        companyData.add(data);

        model.addAttribute("companyData", companyData);
        return "company";
    }

}
