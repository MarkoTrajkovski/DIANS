package com.example.project1.web;

import com.example.project1.db.response.TextAnalysisResult;
import com.example.project1.service.TechAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TechAnalysisController {

    private final TechAnalysisService techAnalysisService;

    @PostMapping("/predict")
    public ResponseEntity<String> technicalAnalysis(@RequestParam(name = "companyId") Long companyId) {
        String response = techAnalysisService.technicalAnalysis(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyze")
    public ResponseEntity<TextAnalysisResult> nlp(@RequestParam(name = "companyId") Long companyId) throws Exception {
        TextAnalysisResult response = techAnalysisService.nlp(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/predict-next-month-price")
    public ResponseEntity<Double> lstm(@RequestParam(name = "companyId") Long companyId) {
        Double response = techAnalysisService.lstm(companyId);
        return ResponseEntity.ok(response);
    }
}
