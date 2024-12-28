package com.example.project1.db.response;

import lombok.Data;

@Data
public class TextAnalysisResult {
    public String recommendation;
    public Double sentimentScore;
}
