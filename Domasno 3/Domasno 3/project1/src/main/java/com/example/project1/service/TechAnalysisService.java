package com.example.project1.service;

import com.example.project1.db.ListedCompanyEntity;
import com.example.project1.db.TransactionHistoryEntity;
import com.example.project1.db.response.TextAnalysisResult;
import com.example.project1.repository.ListedCompanyRepository;
import com.example.project1.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ListedCompanyRepository listedCompanyRepository;

    private final String technicalAnalysisUrl = "http://127.0.0.1:5000/generate_signal";
    private final String nlpUrl = "http://127.0.0.1:5000/analyze";
    private final String lstmUrl = "http://127.0.0.1:8000/predict-next-month-price/";

    public String technicalAnalysis(Long companyId) {
        // Retrieve historical data from the repository
        List<TransactionHistoryEntity> data = transactionHistoryRepository.findByCompanyId(companyId);

        // Prepare the data to send to the Python API
        List<Map<String, Object>> payload = new ArrayList<>();
        for (TransactionHistoryEntity d : data) {
            Map<String, Object> record = new HashMap<>();
            record.put("date", d.getDate().toString());
            record.put("close", d.getLastTransactionPrice());
            record.put("open", (d.getMaxPrice() + d.getMinPrice()) / 2.0);
            record.put("high", d.getMaxPrice());
            record.put("low", d.getMinPrice());
            record.put("volume", d.getQuantity());
            payload.add(record);
        }

        // Create the HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the HTTP entity with the payload
        HttpEntity<List<Map<String, Object>>> requestEntity = new HttpEntity<>(payload, headers);

        // Send the request to the Python API
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                technicalAnalysisUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // Parse the response
        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody != null && responseBody.containsKey("final_signal")) {
            return responseBody.get("final_signal").toString();
        } else {
            throw new RuntimeException("Failed to retrieve a valid signal from the Python API.");
        }
    }

    public TextAnalysisResult nlp(Long companyId) throws Exception {
        ListedCompanyEntity company = listedCompanyRepository.findById(companyId).orElseThrow(() -> new Exception("Company not found"));

        String companyCode = company.getCompanyCode();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                nlpUrl + "?company_code=" + companyCode,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody != null) {
            if (responseBody.containsKey("error")) {
                String errorMessage = (String) responseBody.get("error");
                throw new RuntimeException("Error from Python API: " + errorMessage);
            }

            TextAnalysisResult textAnalysisResult = new TextAnalysisResult();
            textAnalysisResult.sentimentScore = (Double) responseBody.get("sentiment_score");
            textAnalysisResult.recommendation = (String) responseBody.get("recommendation");

            return textAnalysisResult;
        } else {
            throw new RuntimeException("Failed to retrieve sentiment analysis from the Python API.");
        }
    }

    public Double lstm(Long companyId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<TransactionHistoryEntity> data = transactionHistoryRepository.findByCompanyIdAndDateBetween(companyId, LocalDate.now().minusMonths(3), LocalDate.now());;

        Map<String, Object> requestBody = Map.of("data", mapToRequestData(data));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        Map<String, Double> response = restTemplate.postForObject(lstmUrl, requestEntity, Map.class);

        return response != null ? response.get("predicted_next_month_price") : null;
    }

    public static List<Map<String, Object>> mapToRequestData(List<TransactionHistoryEntity> historicalDataEntities) {
        return historicalDataEntities.stream().map(entity -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("date", entity.getDate().toString());
            dataMap.put("average_price", entity.getAveragePrice());
            return dataMap;
        }).collect(Collectors.toList());
    }

}
