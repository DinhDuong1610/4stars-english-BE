package com.fourstars.FourStars.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fourstars.FourStars.domain.response.dictation.NlpAnalysisResponse;

@Service
public class NlpApiService {
    private static final Logger logger = LoggerFactory.getLogger(NlpApiService.class);

    @Value("${nlp.api.url}")
    private String nlpApiUrl;

    private final RestTemplate restTemplate;

    public NlpApiService() {
        this.restTemplate = new RestTemplate();
    }

    public NlpAnalysisResponse getAnalysis(String userText, String correctText) {
        logger.debug("Sending text to NLP API for analysis. User text length: {}", userText.length());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of(
                    "user_text", userText,
                    "correct_text", correctText);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            NlpAnalysisResponse response = restTemplate.postForObject(nlpApiUrl, request, NlpAnalysisResponse.class);

            logger.debug("Received NLP analysis with score: {}", response != null ? response.getScore() : "N/A");
            return response;

        } catch (Exception e) {
            logger.error("Error calling NLP API at {}", nlpApiUrl, e);
            return null;
        }
    }
}