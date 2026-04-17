package com.medisphere.AIsymptomcheck.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisphere.AIsymptomcheck.dto.SymptomCheckRequest;
import com.medisphere.AIsymptomcheck.dto.SymptomCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SymptomService {

    private static final Logger log =
            LoggerFactory.getLogger(SymptomService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-url}")
    private String apiUrl;

    @Value("${groq.api-key}")
    private String apiKey;

    public SymptomService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public SymptomCheckResponse checkSymptoms(SymptomCheckRequest request) {
        String prompt = buildPrompt(request);
        String geminiResponse = callGroqApi(prompt);
        return parseGeminiResponse(geminiResponse);
    }

    // --- Build a structured prompt that forces JSON output ---
    private String buildPrompt(SymptomCheckRequest request) {
        String symptomsList = String.join(", ", request.getSymptoms());

        return """
                You are a medical AI assistant. A patient has reported the following symptoms.
                Analyze them and respond ONLY with a valid JSON object — no extra text, 
                no markdown, no explanation outside the JSON.
                
                Patient details:
                - Age: %s
                - Gender: %s
                - Symptoms: %s
                - Additional info: %s
                
                Respond with this exact JSON structure:
                {
                  "possibleConditions": ["condition1", "condition2", "condition3"],
                  "recommendedSpecialties": ["specialty1", "specialty2"],
                  "urgencyLevel": "LOW" or "MEDIUM" or "HIGH",
                  "generalAdvice": ["advice1", "advice2", "advice3"]
                }
                
                Rules:
                - possibleConditions: list 2-4 possible conditions, most likely first
                - recommendedSpecialties: list 1-3 doctor specialties the patient should see
                - urgencyLevel: LOW (can wait a few days), MEDIUM (see doctor soon), 
                  HIGH (seek immediate care)
                - generalAdvice: list 3-4 practical self-care tips
                - Never diagnose definitively — use words like "possible", "may indicate"
                """.formatted(
                request.getAge(),
                request.getGender(),
                symptomsList,
                request.getAdditionalInfo() != null
                        ? request.getAdditionalInfo() : "None"
        );
    }

    private String callGroqApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );
        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(message),
                "temperature", 0.3
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }
            throw new RuntimeException("Empty response from Groq API");

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to get response from AI service");
        }
    }

    // --- Parse the JSON Gemini returns into our DTO ---
    private SymptomCheckResponse parseGeminiResponse(String rawResponse) {
        try {
            // Clean up in case Gemini adds markdown code fences
            String cleaned = rawResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            List<String> conditions  = new ArrayList<>();
            List<String> specialties = new ArrayList<>();
            List<String> advice      = new ArrayList<>();

            node.path("possibleConditions")
                    .forEach(n -> conditions.add(n.asText()));
            node.path("recommendedSpecialties")
                    .forEach(n -> specialties.add(n.asText()));
            node.path("generalAdvice")
                    .forEach(n -> advice.add(n.asText()));

            String urgency = node.path("urgencyLevel")
                    .asText("MEDIUM");

            return SymptomCheckResponse.builder()
                    .possibleConditions(conditions)
                    .recommendedSpecialties(specialties)
                    .urgencyLevel(urgency)
                    .generalAdvice(advice)
                    .disclaimer("This is an AI-generated preliminary assessment " +
                            "and does not constitute medical advice. " +
                            "Please consult a qualified healthcare professional.")
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI response");
        }
    }
}