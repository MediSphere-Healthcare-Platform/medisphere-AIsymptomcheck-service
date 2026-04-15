package com.medisphere.AIsymptomcheck.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SymptomCheckResponse {

    // e.g. ["Common cold", "Flu", "Strep throat"]
    private List<String> possibleConditions;

    // e.g. ["General Practitioner", "ENT Specialist"]
    private List<String> recommendedSpecialties;

    // e.g. "LOW", "MEDIUM", "HIGH"
    private String urgencyLevel;

    // General advice
    private List<String> generalAdvice;

    // Raw disclaimer
    private String disclaimer;
}